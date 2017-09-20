package org.openlca.io.ilcd.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.models.Connection;
import org.openlca.ilcd.models.DataSetInfo;
import org.openlca.ilcd.models.DownstreamLink;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.models.ModelName;
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

	private Map<Long, Integer> processIDs = new HashMap<>();
	private Map<Long, ProcessDescriptor> processes = new HashMap<>();
	private Map<Long, FlowDescriptor> flows = new HashMap<>();

	public SystemExport(ExportConfig config) {
		this.config = config;
	}

	public Model run(ProductSystem system) throws DataStoreException {
		if (system == null)
			return null;
		if (config.store.contains(Model.class, system.getRefId()))
			return config.store.get(Model.class, system.getRefId());
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
			processes.put(pd.getId(), pd);
			processIDs.put(pd.getId(), processIDs.size());
		}
		FlowDao fDao = new FlowDao(config.db);
		for (FlowDescriptor fd : fDao.getDescriptors()) {
			flows.put(fd.getId(), fd);
		}
	}

	private Model initModel() {
		Model model = new Model();
		DataSetInfo info = Models.dataSetInfo(model);
		info.uuid = system.getRefId();
		ModelName name = Models.modelName(model);
		name.name.add(LangString.of(system.getName(), config.lang));
		if (system.getDescription() != null)
			info.comment
					.add(LangString.of(system.getDescription(), config.lang));
		CategoryConverter conv = new CategoryConverter();
		Classification c = conv.getClassification(system.getCategory());
		if (c != null)
			Models.classifications(model).add(c);
		if (system.getReferenceProcess() != null) {
			long refId = system.getReferenceProcess().getId();
			QuantitativeReference qRef = Models.quantitativeReference(model);
			qRef.refProcess = processIDs.getOrDefault(refId, -1);
		}
		return model;
	}

	private void mapLinks(Model model) throws DataStoreException {
		Technology tech = Models.technology(model);
		Map<Long, ProcessInstance> instances = new HashMap<>();
		for (Long id : system.getProcesses()) {
			ProcessInstance pi = initProcessInstance(id);
			instances.put(id, pi);
			tech.processes.add(pi);
		}
		for (ProcessLink link : system.getProcessLinks()) {
			ProcessInstance pi = instances.get(link.providerId);
			addLink(pi, link);
		}
	}

	private ProcessInstance initProcessInstance(Long id)
			throws DataStoreException {
		ProcessInstance instance = new ProcessInstance();
		instance.id = processIDs.getOrDefault(id, -1);
		ProcessDescriptor d = processes.get(id);
		if (!config.store.contains(Process.class, d.getRefId())) {
			ProcessDao dao = new ProcessDao(config.db);
			ExportDispatch.forwardExportCheck(
					dao.getForId(d.getId()), config);
		}
		instance.process = toRef(d);
		return instance;
	}

	private void addLink(ProcessInstance pi, ProcessLink link) {
		if (pi == null || link == null)
			return;
		FlowDescriptor f = flows.get(link.flowId);
		if (f == null)
			return;
		Connection con = null;
		for (Connection c : pi.connections) {
			if (Objects.equals(c.outputFlow, f.getRefId())) {
				con = c;
				break;
			}
		}
		if (con == null) {
			con = new Connection();
			con.outputFlow = f.getRefId();
			pi.connections.add(con);
		}
		DownstreamLink dl = new DownstreamLink();
		dl.inputFlow = f.getRefId();
		dl.process = processIDs.getOrDefault(link.processId, -1);
		if (dl.process != -1) {
			con.downstreamLinks.add(dl);
		}
	}

	private Ref toRef(ProcessDescriptor d) {
		if (d == null)
			return null;
		Ref ref = new Ref();
		ref.type = DataSetType.PROCESS;
		ref.uuid = d.getRefId();
		ref.uri = "../processes/" + ref.uuid + ".xml";
		ref.version = Version.asString(d.getVersion());
		ref.name.add(LangString.of(d.getName(), config.lang));
		return ref;
	}
}
