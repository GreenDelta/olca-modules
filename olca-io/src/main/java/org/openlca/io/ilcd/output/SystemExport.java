package org.openlca.io.ilcd.output;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.eclipse.persistence.sessions.Connector;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.math.ReferenceAmount;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.models.DataSetInfo;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.models.ModelName;
import org.openlca.ilcd.models.ProcessInstance;
import org.openlca.ilcd.models.QuantitativeReference;
import org.openlca.ilcd.processes.Exchange;
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
		if (config.store.contains(Model.class, system.getRefId()))
			return config.store.get(Model.class, system.getRefId());
		this.system = system;
		log.trace("Run product system export with {}", system);
		if (!canRun()) {
			log.error("System {} is not valid and cannot exported", system);
			return null;
		}
		loadMaps();
		Model model = initModel();
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
			info.comment.add(LangString.of(system.getDescription(), config.lang));
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
		Map<Long, ProcessInstance> instances = new HashMap<>();
		for (Long id : system.getProcesses()) {
			ProcessInstance instance = new ProcessInstance();
			instances.put(id, instance);
			instance.id = processIDs.getOrDefault(id, -1);
			ProcessDescriptor d = processes.get(id);
			checkExport(d);
			instance.process = toRef(d);
		}

	}

	private void checkExport(ProcessDescriptor d)
			throws DataStoreException {
		if (config.store.contains(Process.class, d.getRefId()))
			return;
		ProcessDao dao = new ProcessDao(config.db);
		ExportDispatch.forwardExportCheck(
				dao.getForId(d.getId()), config);
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

	private void exportLinks(ProductModel model) {
		int c = 0;
		ProcessDao processDao = new ProcessDao(config.db);
		FlowDao flowDao = new FlowDao(config.db);
		for (ProcessLink link : this.system.getProcessLinks()) {
			Connector connector = new Connector();
			model.getConnections().add(connector);
			connector.setId(Integer.toString(++c));

			// provider process
			BaseDescriptor provider = processDao.getDescriptor(link.providerId);
			if (provider == null)
				continue;
			connector.setOrigin(provider.getRefId());

			// product flow
			BaseDescriptor flow = flowDao.getDescriptor(link.flowId);
			if (flow == null)
				continue;
			Product product = new Product();
			connector.getProducts().add(product);
			product.setName(flow.getName());
			product.setUuid(flow.getRefId());

			// recipient process
			ConsumedBy consumedBy = new ConsumedBy();
			product.setConsumedBy(consumedBy);
			consumedBy.setFlowUUID(flow.getRefId());
			BaseDescriptor recipient = processDao.getDescriptor(link.processId);
			if (recipient == null)
				continue;
			consumedBy.setProcessId(recipient.getRefId());

			// we do not set the amount field currently because it is
			// the value in the respective process
			// consumedBy.setAmount(link.getRecipientInput().getConvertedResult());
		}
	}

	private void addExchange(Process process) {
		org.openlca.core.model.Exchange refExchange = system
				.getReferenceExchange();
		Exchange exchange = new Exchange();
		process.exchanges.add(exchange);
		exchange.id = 1;
		exchange.direction = ExchangeDirection.OUTPUT;
		Ref flowRef = ExportDispatch.forwardExportCheck(
				refExchange.flow, config);
		exchange.flow = flowRef;
		double refAmount = ReferenceAmount.get(system);
		exchange.meanAmount = refAmount;
		exchange.resultingAmount = refAmount;
	}

	private boolean canRun() {
		if (system == null)
			return false;
		return system.getReferenceExchange() != null
				&& system.getReferenceProcess() != null;
	}

	// TODO: map system parameters
	// private void addParamaters(ProductModel model) {
	// for (Parameter parameter : system.getParameters())
	// addParameter(parameter, model, ParameterScopeValues.PRODUCTMODEL);
	// ParameterDao dao = new ParameterDao(database);
	// try {
	// for (Parameter param : dao.getAllForType(ParameterScope.DATABASE))
	// addParameter(param, model, ParameterScopeValues.GLOBAL);
	// } catch (Exception e) {
	// log.error("Failed to export database paramaters", e);
	// }
	// }
	//
	// private void addParameter(Parameter parameter, ProductModel model,
	// ParameterScopeValues scope) {
	// Expression exp = parameter.getExpression();
	// if (exp == null || parameter.getName() == null)
	// return;
	// org.openlca.ilcd.productmodel.Parameter iParameter = new
	// org.openlca.ilcd.productmodel.Parameter();
	// iParameter.setFormula(exp.getFormula());
	// iParameter.setName(parameter.getName());
	// iParameter.setScope(scope);
	// iParameter.setValue(exp.getValue());
	// model.getParameters().add(iParameter);
	// }

}
