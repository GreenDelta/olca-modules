package org.openlca.io.ilcd.input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProductSystemDao;
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
			ProductSystemDao dao = new ProductSystemDao(database);
			return dao.getForRefId(systemId);
		} catch (Exception e) {
			throw new ImportException("Could not load product system id="
					+ systemId, e);
		}
	}

	private ProductSystem createNew() throws ImportException {
		system = new ProductSystem();
		ProductModel model = ilcdProcessBag.getProductModel();
		system.setRefId(ilcdProcessBag.getId());
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
		system.setCategory(category);
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
				system.getProcesses().add(p.getId());
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
		Exchange refExchange = findRefExchange(refProc, flowId, false);
		system.setReferenceExchange(refExchange);
		system.setTargetAmount(iExchange.getResultingAmount());
		Flow flow = refExchange.getFlow();
		system.setTargetFlowPropertyFactor(flow.getReferenceFactor());
		system.setTargetUnit(getRefUnit(flow.getReferenceFlowProperty()));
	}

	private Exchange findRefExchange(Process refProc, String flowId,
			boolean input) {
		for (Exchange exchange : refProc.getExchanges()) {
			if (exchange.getFlow() == null || exchange.isInput() != input)
				continue;
			if (Objects.equals(exchange.getFlow().getRefId(), flowId))
				return exchange;
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

			// provider process
			Process provider = processes.get(con.getOrigin());
			if (provider == null)
				continue;
			link.setProviderProcess(provider.getId());

			// provider output flow
			Product product = con.getProducts().get(0);
			String flowId = product.getUuid();
			Flow outFlow = findFlow(provider, flowId, false);
			if (outFlow == null)
				continue;
			link.setProviderOutput(outFlow.getId());

			// recipient process
			ConsumedBy consumedBy = product.getConsumedBy();
			Process recipient = processes.get(consumedBy.getProcessId());
			if (recipient == null)
				continue;
			link.setRecipientProcess(recipient.getId());

			// recipient input flow
			Flow inFlow = findFlow(recipient, flowId, true);
			if (inFlow == null || !Objects.equals(outFlow, inFlow))
				continue;
			link.setRecipientInput(inFlow.getId());
			system.getProcessLinks().add(link);
		}
	}

	private Flow findFlow(Process process, String flowRefId, boolean input) {
		if (process == null || flowRefId == null)
			return null;
		for (Exchange e : process.getExchanges()) {
			if (e.getFlow() == null || e.isInput() != input)
				continue;
			if (Objects.equals(e.getFlow().getRefId(), flowRefId))
				return e.getFlow();
		}
		return null;
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
		ParameterType type = ParameterType.PRODUCT_SYSTEM;
		if (iParam.getScope() == ParameterScopeValues.GLOBAL) {
			type = ParameterType.DATABASE;
		}
		org.openlca.core.model.Parameter param = new org.openlca.core.model.Parameter();
		param.setName(iParam.getName());
		param.setType(type);
		param.getExpression().setValue(exp.getValue());
		param.getExpression().setFormula(exp.getFormula());
		return param;
	}

	private void addOrInsert(org.openlca.core.model.Parameter param) {
		if (param.getType() == ParameterType.PRODUCT_SYSTEM) {
			system.getParameters().add(param);
			return;
		}
		try {
			ParameterDao dao = new ParameterDao(database);
			List<org.openlca.core.model.Parameter> params = dao.getAllForName(
					param.getName(), param.getType());
			if (params.isEmpty())
				dao.insert(param);
		} catch (Exception e) {
			log.error("Failed to store parameter in database", e);
		}
	}

}
