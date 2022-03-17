package org.openlca.io.ilcd.input.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.models.ProcessInstance;
import org.openlca.ilcd.models.Technology;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.Models;
import org.openlca.io.ilcd.input.ImportConfig;
import org.openlca.io.ilcd.input.ProcessImport;
import org.openlca.util.Strings;

/**
 * Imports an eILCD model as product system into an openLCA database.
 */
public class ModelImport {

	private final ImportConfig config;
	private ProductSystem system;
	private int connectorCount = 0;

	public ModelImport(ImportConfig config) {
		this.config = config;
	}

	public ProductSystem run(Model model) {
		if (model == null)
			return null;
		var dao = new ProductSystemDao(config.db());
		system = dao.getForRefId(model.getUUID());
		if (system != null)
			return system;
		String origin = Models.getOrigin(model);
		if (Strings.nullOrEqual("openLCA", origin)
			|| !config.hasGabiGraphSupport()) {
			system = new ProductSystem();
			IO.mapMetaData(model, system);
			String[] path = Categories.getPath(model);
			system.category = new CategoryDao(config.db())
				.sync(ModelType.PRODUCT_SYSTEM, path);
			mapModel(model);
			system = dao.insert(system);
			config.log().imported(system);
			return system;
		} else {
			Graph g = Graph.build(model, config.db());
			g = Transformation.on(g);
			return new GraphSync(config).sync(model, g);
		}
	}

	private void mapModel(Model m) {
		Technology tech = Models.getTechnology(m);
		if (tech == null)
			return;
		var processes = syncProcesses(m, tech);
		var flows = collectFlows(tech);
		for (var pi : tech.processes) {
			var outProcess = processes.get(pi.id);
			if (outProcess == null)
				continue;
			for (var con : pi.connections) {
				var outFlow = flows.get(con.outputFlow);
				if (outFlow == null)
					continue;
				for (var link : con.downstreamLinks) {
					var inFlow = flows.get(link.inputFlow);
					var inProcess = processes.get(link.process);
					if (inFlow == null || inProcess == null)
						continue;
					if (Objects.equals(inFlow, outFlow)) {
						addLink(outProcess, inProcess, inFlow, link.linkedExchange);
					} else {
						var connector = connector(outFlow, inFlow);
						addLink(outProcess, connector, outFlow, null);
						addLink(connector, inProcess, inFlow, null);
					}
				}
			}
		}
	}

	private Map<Integer, Process> syncProcesses(Model m, Technology tech) {
		var qRef = Models.getQuantitativeReference(m);
		int refProcess = -1;
		if (qRef != null && qRef.refProcess != null) {
			refProcess = qRef.refProcess;
		}
		var map = new HashMap<Integer, Process>();
		for (var pi : tech.processes) {
			if (pi.process == null)
				continue;
			var process = ProcessImport.get(config, pi.process.uuid);
			if (process == null)
				continue;
			if (refProcess == pi.id) {
				mapRefProcess(pi, process);
			}
			addParameterRedefs(pi, process);
			system.processes.add(process.id);
			map.put(pi.id, process);
		}
		return map;
	}

	private void addParameterRedefs(ProcessInstance pi, Process p) {
		for (var param : pi.parameters) {
			if (param.name == null || param.value == null)
				continue;
			var redef = new ParameterRedef();
			redef.contextId = p.id;
			redef.contextType = ModelType.PROCESS;
			redef.name = param.name;
			redef.value = param.value;
			IO.parametersSetOf(system).add(redef);
		}
	}

	private void mapRefProcess(ProcessInstance pi, Process process) {
		if (pi == null || process == null)
			return;
		system.referenceProcess = process;
		var qRef = process.quantitativeReference;
		if (qRef == null)
			return;
		system.referenceExchange = qRef;
		system.targetAmount = qRef.amount;
		system.targetFlowPropertyFactor = qRef.flowPropertyFactor;
		system.targetUnit = qRef.unit;
	}

	/**
	 * Collect the flows that are used in the process links. This function must
	 * be called after all processes are imported.
	 */
	private Map<String, Flow> collectFlows(Technology tech) {
		var usedFlows = new HashSet<String>();
		for (var pi : tech.processes) {
			for (var con : pi.connections) {
				usedFlows.add(con.outputFlow);
				for (var link : con.downstreamLinks) {
					usedFlows.add(link.inputFlow);
				}
			}
		}
		var dao = new FlowDao(config.db());
		var map = new HashMap<String, Flow>();
		for (var flow : dao.getForRefIds(usedFlows)) {
			map.put(flow.refId, flow);
		}
		return map;
	}

	private void addLink(Process out, Process in, Flow flow,
	                     Integer exchangeId) {
		boolean isWaste = flow.flowType == FlowType.WASTE_FLOW;
		var link = new ProcessLink();
		link.flowId = flow.id;
		link.providerId = isWaste ? in.id : out.id;
		link.processId = isWaste ? out.id : in.id;
		Exchange exchange = null;
		Process linked = isWaste ? out : in;
		for (Exchange e : linked.exchanges) {
			if (e.isInput == isWaste || !Objects.equals(flow, e.flow))
				continue;
			exchange = e;
			if (exchangeId == null || exchangeId == e.internalId) {
				break;
			}
		}
		if (exchange == null)
			return;
		link.exchangeId = exchange.id;
		system.processLinks.add(link);
	}

	/**
	 * Creates a connector process for the given input flow and output flow. In
	 * openLCA we can only link processes via the same flow. Therefore, if the
	 * linked exchanges in an eILCD model have different flows, we need to
	 * create such a process. Note that the input flow is the output and the
	 * output flow the input in the connector process.
	 */
	private Process connector(Flow outFlow, Flow inFlow) {
		Process p = new Process();
		connectorCount++;
		p.name = "Connector " + connectorCount;
		p.refId = UUID.randomUUID().toString();
		var input = p.input(outFlow, 1);
		var output = p.output(inFlow, 1);
		p.quantitativeReference = outFlow.flowType == FlowType.WASTE_FLOW
			? input
			: output;
		p = config.db().insert(p);
		config.log().warn(p,
			"created connector process to map eILCD link with different flows");
		system.processes.add(p.id);
		return p;
	}
}
