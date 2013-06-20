package org.openlca.io.ilcd.input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Expression;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.productmodel.Connector;
import org.openlca.ilcd.productmodel.ConsumedBy;
import org.openlca.ilcd.productmodel.Parameter;
import org.openlca.ilcd.productmodel.ParameterScopeValues;
import org.openlca.ilcd.productmodel.ProcessNode;
import org.openlca.ilcd.productmodel.Product;
import org.openlca.ilcd.productmodel.ProductModel;
import org.openlca.ilcd.util.ProcessBag;
import org.openlca.ilcd.util.ProcessInfoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;
	private DataStore dataStore;
	private ProcessBag ilcdProcessBag;
	private ProductSystem system;

	public SystemImport(DataStore dataStore, IDatabase database) {
		this.database = database;
		this.dataStore = dataStore;
	}

	public ProductSystem run(org.openlca.ilcd.processes.Process ilcdProcess)
			throws ImportException {
		ilcdProcessBag = new ProcessBag(ilcdProcess);
		if (!ilcdProcessBag.hasProductModel())
			return null;
		ProductSystem system = findExisting(ilcdProcessBag.getId());
		if (system != null)
			return system;
		return createNew();
	}

	private ProductSystem findExisting(String systemId) throws ImportException {
		try {
			return database.createDao(ProductSystem.class).getForId(systemId);
		} catch (Exception e) {
			throw new ImportException("Could not load product system id="
					+ systemId, e);
		}
	}

	private ProductSystem createNew() throws ImportException {
		system = new ProductSystem();
		ProductModel model = ilcdProcessBag.getProductModel();
		system.setId(ilcdProcessBag.getId());
		system.setName(model.getName());
		importAndSetCategory();
		mapContent();
		try {
			database.createDao(ProductSystem.class).insert(system);
			return system;
		} catch (Exception e) {
			throw new ImportException("Failed to save in database", e);
		}
	}

	private void importAndSetCategory() throws ImportException {
		CategoryImport categoryImport = new CategoryImport(database,
				ModelType.PRODUCT_SYSTEM);
		Category category = categoryImport.run(ilcdProcessBag
				.getSortedClasses());
		system.setCategoryId(category.getId());
	}

	private void mapContent() throws ImportException {
		Map<String, Process> processes = addProcesses();
		setRefProcess(processes);
		addProcessLinks(processes);
		addParameters();
	}

	private Map<String, Process> addProcesses() throws ImportException {
		HashMap<String, Process> result = new HashMap<>();
		ProductModel model = ilcdProcessBag.getProductModel();
		for (ProcessNode node : model.getNodes()) {
			String processId = node.getUuid();
			ProcessImport processImport = new ProcessImport(dataStore, database);
			Process p = processImport.run(processId);
			if (p != null) {
				result.put(processId, p);
				system.getProcesses().add(p);
			}
		}
		return result;
	}

	private void setRefProcess(Map<String, Process> processes)
			throws ImportException {
		String refProcessId = new ProcessInfoExtension(ilcdProcessBag
				.getValue().getProcessInformation()).getModelRefProcess();
		Process refProc = processes.get(refProcessId);
		system.setReferenceProcess(refProc);
		org.openlca.ilcd.processes.Exchange iExchange = ilcdProcessBag
				.getExchanges().get(0);
		String flowId = iExchange.getFlow().getUuid();
		Exchange refExchange = findExchange(refProc, flowId, false);
		system.setReferenceExchange(refExchange);
		system.setTargetAmount(iExchange.getResultingAmount());
		Flow flow = refExchange.getFlow();
		system.setTargetFlowPropertyFactor(flow.getReferenceFactor());
		system.setTargetUnit(getRefUnit(flow.getReferenceFlowProperty()));
	}

	private Exchange findExchange(Process process, String flowId, boolean input) {
		for (Exchange e : process.getExchanges()) {
			if (e.getFlow().getId().equals(flowId) && e.isInput() == input)
				return e;
		}
		return null;
	}

	private Unit getRefUnit(FlowProperty prop) throws ImportException {
		try {
			UnitGroup group = prop.getUnitGroup();
			return group.getReferenceUnit();
		} catch (Exception e) {
			throw new ImportException("Could not load ref-unit of property "
					+ prop, e);
		}
	}

	private void addProcessLinks(Map<String, Process> processes) {
		ProductModel model = ilcdProcessBag.getProductModel();
		for (Connector con : model.getConnections()) {
			ProcessLink link = new ProcessLink();
			link.setId(UUID.randomUUID().toString());
			Process provider = processes.get(con.getOrigin());
			link.setProviderProcess(provider);
			Product product = con.getProducts().get(0);
			String flowId = product.getUuid();
			link.setProviderOutput(findExchange(provider, flowId, false));
			ConsumedBy consumedBy = product.getConsumedBy();
			Process recipient = processes.get(consumedBy.getProcessId());
			link.setRecipientProcess(recipient);
			link.setRecipientInput(findExchange(recipient, flowId, true));
			if (valid(link))
				system.getProcessLinks().add(link);
			else
				log.warn("Could not add process link {} - invalid", link);
		}
	}

	private boolean valid(ProcessLink link) {
		return link.getId() != null && link.getProviderProcess() != null
				&& link.getProviderOutput() != null
				&& link.getRecipientProcess() != null
				&& link.getRecipientInput() != null;
	}

	private void addParameters() {
		for (Parameter iParam : ilcdProcessBag.getProductModel()
				.getParameters()) {
			if (!valid(iParam))
				continue;
			org.openlca.core.model.Parameter oParam = convert(iParam);
			addOrInsert(oParam);
		}
	}

	private boolean valid(Parameter iParam) {
		return iParam.getFormula() != null && iParam.getValue() != null
				&& iParam.getName() != null && iParam.getScope() != null;
	}

	private org.openlca.core.model.Parameter convert(Parameter iParam) {
		Expression exp = new Expression(iParam.getFormula(), iParam.getValue());
		String owner = system.getId();
		ParameterType type = ParameterType.PRODUCT_SYSTEM;
		if (iParam.getScope() == ParameterScopeValues.GLOBAL) {
			owner = null;
			type = ParameterType.DATABASE;
		}
		org.openlca.core.model.Parameter param = new org.openlca.core.model.Parameter(
				UUID.randomUUID().toString(), exp, type, owner);
		param.setName(iParam.getName());
		return param;
	}

	private void addOrInsert(org.openlca.core.model.Parameter param) {
		if (param.getType() == ParameterType.PRODUCT_SYSTEM) {
			system.getParameters().add(param);
			return;
		}
		try {
			ParameterDao dao = new ParameterDao(database.getEntityFactory());
			List<org.openlca.core.model.Parameter> params = dao.getAllForName(
					param.getName(), param.getType());
			if (params.isEmpty())
				dao.insert(param);
		} catch (Exception e) {
			log.error("Failed to store parameter in database", e);
		}
	}

}
