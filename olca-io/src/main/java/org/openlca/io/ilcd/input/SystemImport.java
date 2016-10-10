package org.openlca.io.ilcd.input;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.productmodel.Connector;
import org.openlca.ilcd.productmodel.ConsumedBy;
import org.openlca.ilcd.productmodel.Parameter;
import org.openlca.ilcd.productmodel.ProcessNode;
import org.openlca.ilcd.productmodel.Product;
import org.openlca.ilcd.productmodel.ProductModel;
import org.openlca.ilcd.util.ProcessBag;
import org.openlca.ilcd.util.ProcessInfoExtension;

public class SystemImport {

	private final ImportConfig config;
	private ProcessBag ilcdProcessBag;
	private ProductSystem system;

	public SystemImport(ImportConfig config) {
		this.config = config;
	}

	public ProductSystem run(org.openlca.ilcd.processes.Process ilcdProcess)
			throws ImportException {
		ilcdProcessBag = new ProcessBag(ilcdProcess, config.langs);
		if (!ilcdProcessBag.hasProductModel())
			return null;
		ProductSystem system = findExisting(ilcdProcessBag.getId());
		if (system != null)
			return system;
		return createNew();
	}

	private ProductSystem findExisting(String systemId) throws ImportException {
		try {
			ProductSystemDao dao = new ProductSystemDao(config.db);
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
			config.db.createDao(ProductSystem.class).insert(system);
			return system;
		} catch (Exception e) {
			throw new ImportException("Failed to save in database", e);
		}
	}

	private void importAndSetCategory() throws ImportException {
		CategoryImport categoryImport = new CategoryImport(config,
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
			ProcessImport processImport = new ProcessImport(config);
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
				.getValue().processInfo).getModelRefProcess();
		Process refProc = processes.get(refProcessId);
		system.setReferenceProcess(refProc);
		org.openlca.ilcd.processes.Exchange iExchange = ilcdProcessBag
				.getExchanges().get(0);
		String flowId = iExchange.flow.uuid;
		Exchange refExchange = findRefExchange(refProc, flowId, false);
		system.setReferenceExchange(refExchange);
		system.setTargetAmount(iExchange.resultingAmount);
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
			link.providerId = provider.getId();

			// provider output flow
			Product product = con.getProducts().get(0);
			String flowId = product.getUuid();
			Exchange output = findExchange(provider, flowId, false);
			if (output == null)
				continue;
			link.flowId = output.getFlow().getId();

			// linked exchange
			ConsumedBy consumedBy = product.getConsumedBy();
			Process recipient = processes.get(consumedBy.getProcessId());
			if (recipient == null)
				continue;
			link.processId = recipient.getId();
			Exchange input = findExchange(recipient, flowId, true);
			if (input == null)
				continue;
			link.exchangeId = input.getId();

			system.getProcessLinks().add(link);
		}
	}

	private Exchange findExchange(Process p, String flowRefId, boolean input) {
		if (p == null || flowRefId == null)
			return null;
		for (Exchange e : p.getExchanges()) {
			if (e.getFlow() == null || e.isInput() != input)
				continue;
			if (Objects.equals(e.getFlow().getRefId(), flowRefId))
				return e;
		}
		return null;
	}

	private void addParameters() {
		for (Parameter iParam : ilcdProcessBag.getProductModel()
				.getParameters()) {
			if (!valid(iParam))
				continue;
			// TODO: parameter handling
			// org.openlca.core.model.Parameter oParam = convert(iParam);
			// addOrInsert(oParam);
		}
	}

	private boolean valid(Parameter iParam) {
		return iParam.getFormula() != null && iParam.getValue() != null
				&& iParam.getName() != null && iParam.getScope() != null;
	}

	// private ParameterRedef convert(Parameter iParam) {
	// ParameterRedef redef = new ParameterRedef();
	// redef.setName(iParam.getName());
	// redef.setValue(iParam.getValue());
	// return redef;
	// }

	// private void addOrInsert(org.openlca.core.model.Parameter param) {
	// if (param.getScope() == ParameterScope.PRODUCT_SYSTEM) {
	// system.getParameters().add(param);
	// return;
	// }
	// try {
	// ParameterDao dao = new ParameterDao(database);
	// List<org.openlca.core.model.Parameter> params = dao.getAllForName(
	// param.getName(), param.getScope());
	// if (params.isEmpty())
	// dao.insert(param);
	// } catch (Exception e) {
	// log.error("Failed to store parameter in database", e);
	// }
	// }

}
