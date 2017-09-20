package org.openlca.io.ilcd.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.models.Connection;
import org.openlca.ilcd.models.DownstreamLink;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.models.ModelName;
import org.openlca.ilcd.models.ProcessInstance;
import org.openlca.ilcd.models.Publication;
import org.openlca.ilcd.models.QuantitativeReference;
import org.openlca.ilcd.models.Technology;
import org.openlca.ilcd.util.ClassList;
import org.openlca.ilcd.util.Models;
import org.openlca.util.Strings;

public class SystemImport {

	private final ImportConfig config;
	private ProductSystem system;
	private int connectorCount = 0;

	public SystemImport(ImportConfig config) {
		this.config = config;
	}

	public ProductSystem run(Model model) throws ImportException {
		if (model == null)
			return null;
		try {
			ProductSystemDao dao = new ProductSystemDao(config.db);
			system = dao.getForRefId(model.getUUID());
			if (system != null)
				return system;
			system = new ProductSystem();
			system.setRefId(model.getUUID());
			mapMetaData(model);
			mapModel(model);
			return dao.insert(system);
		} catch (Exception e) {
			throw new ImportException("Failed to get/create product system", e);
		}
	}

	private void mapMetaData(Model model) throws ImportException {
		system.setName(getName(model));
		CategoryImport categoryImport = new CategoryImport(config,
				ModelType.PRODUCT_SYSTEM);
		Category category = categoryImport.run(ClassList.sortedList(model));
		system.setCategory(category);
		Publication pub = Models.getPublication(model);
		if (pub != null && pub.version != null) {
			system.setVersion(Version.fromString(pub.version).getValue());
		}
	}

	@SuppressWarnings("unchecked")
	private String getName(Model m) {
		ModelName mn = Models.getModelName(m);
		if (mn == null)
			return "";
		List<?>[] parts = new List<?>[] { mn.name, mn.technicalDetails,
				mn.mixAndLocation, mn.flowProperties };
		String name = "";
		for (List<?> part : parts) {
			String s = LangString.getFirst((List<LangString>) part,
					config.langs);
			if (Strings.nullOrEmpty(s))
				continue;
			if (name.length() > 0)
				name += "; ";
			name += s.trim();
		}
		return name;
	}

	private void mapModel(Model m) throws ImportException {
		Technology tech = Models.getTechnology(m);
		if (tech == null)
			return;
		Map<Integer, Process> processes = insertProcesses(m, tech);
		Map<String, Flow> flows = collectFlows(tech);
		for (ProcessInstance pi : tech.processes) {
			Process out = processes.get(pi.id);
			if (out == null)
				continue;
			for (Connection con : pi.connections) {
				Flow outFlow = flows.get(con.outputFlow);
				if (outFlow == null)
					continue;
				for (DownstreamLink link : con.downstreamLinks) {
					Flow inFlow = flows.get(link.inputFlow);
					Process in = processes.get(link.process);
					if (inFlow == null || in == null)
						continue;
					if (Objects.equals(inFlow, outFlow)) {
						addLink(out, in, inFlow);
					} else {
						Process connector = connector(inFlow, outFlow);
						addLink(out, connector, outFlow);
						addLink(connector, in, inFlow);
					}
				}
			}
		}
	}

	private Map<Integer, Process> insertProcesses(Model m, Technology tech)
			throws ImportException {
		QuantitativeReference qRef = Models.getQuantitativeReference(m);
		int refProcess = -1;
		if (qRef != null && qRef.refProcess != null)
			refProcess = qRef.refProcess.intValue();
		Map<Integer, Process> map = new HashMap<>();
		for (ProcessInstance pi : tech.processes) {
			if (pi.process == null)
				continue;
			ProcessImport pImport = new ProcessImport(config);
			Process p = pImport.run(pi.process.uuid);
			if (refProcess == pi.id) {
				mapRefProcess(pi, p);
			}
			system.getProcesses().add(p.getId());
			map.put(pi.id, p);
		}
		return map;
	}

	private void mapRefProcess(ProcessInstance pi, Process process) {
		if (pi == null || process == null)
			return;
		system.setReferenceProcess(process);
		Exchange e = findRefExchange(pi, process);
		if (e == null)
			return;
		system.setReferenceExchange(e);
		system.setTargetAmount(e.amount);
		system.setTargetFlowPropertyFactor(e.flowPropertyFactor);
		system.setTargetUnit(e.unit);
	}

	private Exchange findRefExchange(ProcessInstance pi, Process process) {
		for (Connection con : pi.connections) {
			String flowID = con.outputFlow;
			if (flowID == null)
				continue;
			for (Exchange e : process.getExchanges()) {
				if (e.flow == null)
					continue;
				if (Objects.equals(flowID, e.flow.getRefId()))
					return e;
			}
		}
		return null;
	}

	/**
	 * Collect the flows that are used in the process links. This function must
	 * be called after all processes are imported.
	 */
	private Map<String, Flow> collectFlows(Technology tech) {
		Set<String> usedFlows = new HashSet<>();
		for (ProcessInstance pi : tech.processes) {
			for (Connection con : pi.connections) {
				usedFlows.add(con.outputFlow);
				for (DownstreamLink link : con.downstreamLinks) {
					usedFlows.add(link.inputFlow);
				}
			}
		}
		FlowDao dao = new FlowDao(config.db);
		Map<String, Flow> m = new HashMap<>();
		for (Flow f : dao.getForRefIds(usedFlows)) {
			m.put(f.getRefId(), f);
		}
		return m;
	}

	private void addLink(Process out, Process in, Flow flow) {
		boolean isWaste = flow.getFlowType() == FlowType.WASTE_FLOW;
		ProcessLink link = new ProcessLink();
		link.flowId = flow.getId();
		link.providerId = isWaste ? in.getId() : out.getId();
		link.processId = isWaste ? out.getId() : in.getId();
		Exchange exchange = null;
		Process linked = isWaste ? out : in;
		for (Exchange e : linked.getExchanges()) {
			if (e.isInput == isWaste || !Objects.equals(flow, e.flow))
				continue;
			exchange = e;
			break;
		}
		if (exchange == null)
			return;
		link.exchangeId = exchange.getId();
		system.getProcessLinks().add(link);
	}

	private Process connector(Flow inFlow, Flow outFlow) {
		Process p = new Process();
		connectorCount++;
		p.setName("Connector " + connectorCount);
		p.setRefId(UUID.randomUUID().toString());
		Exchange input = exchange(inFlow, p, true);
		Exchange output = exchange(outFlow, p, false);
		if (inFlow.getFlowType() == FlowType.WASTE_FLOW) {
			p.setQuantitativeReference(input);
		} else {
			p.setQuantitativeReference(output);
		}
		ProcessDao dao = new ProcessDao(config.db);
		return dao.insert(p);
	}

	private Exchange exchange(Flow flow, Process p, boolean isInput) {
		Exchange e = new Exchange();
		e.isInput = isInput;
		e.amount = 1.0;
		e.flow = flow;
		e.flowPropertyFactor = flow.getReferenceFactor();
		e.unit = getRefUnit(flow);
		p.getExchanges().add(e);
		return e;
	}

	private Unit getRefUnit(Flow flow) {
		if (flow == null)
			return null;
		FlowPropertyFactor fpf = flow.getReferenceFactor();
		if (fpf == null || fpf.getFlowProperty() == null)
			return null;
		UnitGroup ug = fpf.getFlowProperty().getUnitGroup();
		if (ug == null)
			return null;
		return ug.getReferenceUnit();
	}

}
