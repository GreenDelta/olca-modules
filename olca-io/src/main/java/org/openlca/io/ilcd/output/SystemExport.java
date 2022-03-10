package org.openlca.io.ilcd.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.models.Connection;
import org.openlca.ilcd.models.DataSetInfo;
import org.openlca.ilcd.models.DownstreamLink;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.models.ModelName;
import org.openlca.ilcd.models.Modelling;
import org.openlca.ilcd.models.Parameter;
import org.openlca.ilcd.models.ProcessInstance;
import org.openlca.ilcd.models.QuantitativeReference;
import org.openlca.ilcd.models.Technology;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemExport {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final ExportConfig config;
	private ProductSystem system;

	private final Map<Long, Integer> processIDs = new HashMap<>();
	private final Map<Long, Integer> exchangeIDs = new HashMap<>();
	private final Map<Long, ProcessDescriptor> processes = new HashMap<>();
	private final Map<Long, FlowDescriptor> flows = new HashMap<>();

	public SystemExport(ExportConfig config) {
		this.config = config;
	}

	public Model run(ProductSystem system) {
		if (system == null)
			return null;
		if (config.store.contains(Model.class, system.refId))
			return config.store.get(Model.class, system.refId);
		this.system = system;
		log.trace("Run product system export with {}", system);
		loadMaps();
		Model model = initModel();
		mapLinks(model);
		config.store.put(model);
		this.system = null;
		return model;
	}

	private void loadMaps() {
		ProcessDao pDao = new ProcessDao(config.db);
		for (ProcessDescriptor pd : pDao.getDescriptors()) {
			processes.put(pd.id, pd);
			processIDs.put(pd.id, processIDs.size());
		}
		FlowDao fDao = new FlowDao(config.db);
		for (FlowDescriptor fd : fDao.getDescriptors()) {
			flows.put(fd.id, fd);
		}
		Set<Long> exchanges = system.processLinks.stream()
				.map(link -> link.exchangeId)
				.collect(Collectors.toSet());
		String query = "SELECT id, internal_id FROM tbl_exchanges";
		try {
			NativeSql.on(config.db).query(query, r -> {
				long id = r.getLong(1);
				if (exchanges.contains(id)) {
					exchangeIDs.put(id, r.getInt(2));
				}
				return true;
			});
		} catch (Exception e) {
			log.error("Failed to get internal exchange IDs", e);
		}
	}

	private Model initModel() {
		Model model = new Model();
		Models.setOrigin(model, "openLCA");
		model.version = "1.1";
		model.locations = "../ILCDLocations.xml";
		DataSetInfo info = Models.forceDataSetInfo(model);
		info.uuid = system.refId;
		ModelName name = Models.forceModelName(model);
		name.name.add(LangString.of(system.name, config.lang));
		if (system.description != null) {
			info.comment
					.add(LangString.of(system.description, config.lang));
		}
		CategoryConverter conv = new CategoryConverter();
		Classification c = conv.getClassification(system.category);
		if (c != null)
			Models.forceClassifications(model).add(c);
		if (system.referenceProcess != null) {
			long refId = system.referenceProcess.id;
			QuantitativeReference qRef = Models.forceQuantitativeReference(model);
			qRef.refProcess = processIDs.getOrDefault(refId, -1);
		}
		Models.forcePublication(model).version = Version
				.asString(system.version);
		model.modelling = new Modelling();
		return model;
	}

	private void mapLinks(Model model) {
		Technology tech = Models.forceTechnology(model);
		Map<Long, ProcessInstance> instances = new HashMap<>();
		for (Long id : system.processes) {
			if (id == null)
				continue;
			ProcessInstance pi = initProcessInstance(id);
			instances.put(id, pi);
			tech.processes.add(pi);
		}
		for (ProcessLink link : system.processLinks) {
			FlowDescriptor flow = flows.get(link.flowId);
			if (flow == null)
				continue;
			if (flow.flowType == FlowType.PRODUCT_FLOW) {
				ProcessInstance pi = instances.get(link.providerId);
				addLink(pi, link, flow);
			} else if (flow.flowType == FlowType.WASTE_FLOW) {
				ProcessInstance pi = instances.get(link.processId);
				addLink(pi, link, flow);
			}
		}
	}

	private ProcessInstance initProcessInstance(long id) {
		var pi = new ProcessInstance();
		pi.id = processIDs.getOrDefault(id, -1);
		var d = processes.get(id);
		if (!config.store.contains(Process.class, d.refId)) {
			var dao = new ProcessDao(config.db);
			Export.of(dao.getForId(d.id), config);
		}
		pi.process = toRef(d);

		// process parameters
		if (system.parameterSets.isEmpty())
			return pi;
		var set = system.parameterSets.stream()
			.filter(s -> s.isBaseline)
			.findAny()
			.orElse(system.parameterSets.get(0));
		for (var redef : set.parameters) {
			Long context = redef.contextId;
			if (redef.contextId == null || context != id)
				continue;
			var param = new Parameter();
			param.name = redef.name;
			param.value = redef.value;
			pi.parameters.add(param);
		}
		return pi;
	}

	private void addLink(ProcessInstance pi, ProcessLink link,
			FlowDescriptor flow) {
		if (pi == null || link == null || flow == null)
			return;
		Connection con = null;
		for (Connection c : pi.connections) {
			if (Objects.equals(c.outputFlow, flow.refId)) {
				con = c;
				break;
			}
		}
		if (con == null) {
			con = new Connection();
			con.outputFlow = flow.refId;
			pi.connections.add(con);
		}
		DownstreamLink dl = new DownstreamLink();
		dl.inputFlow = flow.refId;
		dl.linkedExchange = exchangeIDs.get(link.exchangeId);
		long linkProcess = 0L;
		if (flow.flowType == FlowType.PRODUCT_FLOW) {
			linkProcess = link.processId;
		} else if (flow.flowType == FlowType.WASTE_FLOW) {
			linkProcess = link.providerId;
		}
		dl.process = processIDs.getOrDefault(linkProcess, -1);
		if (dl.process != -1) {
			con.downstreamLinks.add(dl);
		}
	}

	private Ref toRef(ProcessDescriptor d) {
		if (d == null)
			return null;
		Ref ref = new Ref();
		ref.type = DataSetType.PROCESS;
		ref.uuid = d.refId;
		ref.uri = "../processes/" + ref.uuid + ".xml";
		ref.version = Version.asString(d.version);
		ref.name.add(LangString.of(d.name, config.lang));
		return ref;
	}
}
