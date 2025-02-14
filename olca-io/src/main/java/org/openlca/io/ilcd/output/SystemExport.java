package org.openlca.io.ilcd.output;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.models.Connection;
import org.openlca.ilcd.models.DownstreamLink;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.models.Parameter;
import org.openlca.ilcd.models.ProcessInstance;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SystemExport {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Export exp;
	private final ProductSystem system;

	private final Map<Long, Integer> processIDs = new HashMap<>();
	private final Map<Long, Integer> exchangeIDs = new HashMap<>();
	private final Map<Long, ProcessDescriptor> processes = new HashMap<>();
	private final Map<Long, FlowDescriptor> flows = new HashMap<>();

	public SystemExport(Export exp, ProductSystem system) {
		this.exp = exp;
		this.system = system;
	}

	public void write() {
		if (system == null || exp.store.contains(Model.class, system.refId))
			return;
		log.trace("Run product system export with {}", system);
		loadMaps();
		Model model = initModel();
		mapLinks(model);
		exp.store.put(model);
	}

	private void loadMaps() {
		ProcessDao pDao = new ProcessDao(exp.db);
		for (ProcessDescriptor pd : pDao.getDescriptors()) {
			processes.put(pd.id, pd);
			processIDs.put(pd.id, processIDs.size());
		}
		FlowDao fDao = new FlowDao(exp.db);
		for (FlowDescriptor fd : fDao.getDescriptors()) {
			flows.put(fd.id, fd);
		}
		Set<Long> exchanges = system.processLinks.stream()
				.map(link -> link.exchangeId)
				.collect(Collectors.toSet());
		String query = "SELECT id, internal_id FROM tbl_exchanges";
		try {
			NativeSql.on(exp.db).query(query, r -> {
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
		var model = new Model()
				.withLocations("../ILCDLocations.xml");
		Models.setOrigin(model, "openLCA");

		var info = model.withInfo()
				.withDataSetInfo()
				.withUUID(system.refId);
		var name = info.withModelName();
		exp.add(name::withBaseName, system.name);
		exp.add(info::withComment, system.description);
		Categories.toClassification(system.category, info::withClassifications);

		if (system.referenceProcess != null) {
			long refId = system.referenceProcess.id;
			model.withInfo()
					.withQuantitativeReference()
					.withRefProcess(processIDs.getOrDefault(refId, -1));
		}
		model.withAdminInfo()
				.withPublication()
				.withVersion(Version.asString(system.version));
		return model;
	}

	private void mapLinks(Model model) {
		var tech = model.withInfo().withTechnology();
		Map<Long, ProcessInstance> instances = new HashMap<>();
		for (Long id : system.processes) {
			if (id == null)
				continue;
			var pi = initProcessInstance(id);
			instances.put(id, pi);
			tech.withProcesses().add(pi);
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
		var pi = new ProcessInstance()
				.withId(processIDs.getOrDefault(id, -1));
		var d = processes.get(id);
		if (!exp.store.contains(Process.class, d.refId)) {
			var dao = new ProcessDao(exp.db);
			exp.write(dao.getForId(d.id));
		}
		pi.withProcess(toRef(d));

		// process parameters
		if (system.parameterSets.isEmpty())
			return pi;
		var set = system.parameterSets.stream()
				.filter(s -> s.isBaseline)
				.findAny()
				.orElse(system.parameterSets.getFirst());
		for (var redef : set.parameters) {
			Long context = redef.contextId;
			if (redef.contextId == null || context != id)
				continue;
			var param = new Parameter()
					.withName(redef.name)
					.withValue(redef.value);
			pi.withParameters().add(param);
		}
		return pi;
	}

	private void addLink(ProcessInstance pi, ProcessLink link,
			FlowDescriptor flow) {
		if (pi == null || link == null || flow == null)
			return;
		Connection con = null;
		for (Connection c : pi.getConnections()) {
			if (Objects.equals(c.getOutputFlow(), flow.refId)) {
				con = c;
				break;
			}
		}
		if (con == null) {
			con = new Connection().withOutputFlow(flow.refId);
			pi.withConnections().add(con);
		}
		var dl = new DownstreamLink()
				.withInputFlow(flow.refId)
				.withLinkedExchange(exchangeIDs.get(link.exchangeId));
		long linkProcess = 0L;
		if (flow.flowType == FlowType.PRODUCT_FLOW) {
			linkProcess = link.processId;
		} else if (flow.flowType == FlowType.WASTE_FLOW) {
			linkProcess = link.providerId;
		}
		dl.withProcess(processIDs.getOrDefault(linkProcess, -1));
		if (dl.getProcess() != -1) {
			con.withDownstreamLinks().add(dl);
		}
	}

	private Ref toRef(ProcessDescriptor d) {
		if (d == null)
			return null;
		Ref ref = new Ref()
				.withType(DataSetType.PROCESS)
				.withUUID(d.refId)
				.withUri("../processes/" + d.refId + ".xml")
				.withVersion(Version.asString(d.version));
		exp.add(ref::withName, d.name);
		return ref;
	}
}
