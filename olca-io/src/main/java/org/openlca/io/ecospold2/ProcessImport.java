package org.openlca.io.ecospold2;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openlca.core.database.BaseDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.ecospold2.Activity;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.ElementaryExchange;
import org.openlca.ecospold2.IntermediateExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;
	private FlowHandler flowHandler;
	private Map<String, Category> processCategories = new HashMap<>();

	public ProcessImport(IDatabase database) {
		this.database = database;
		this.flowHandler = new FlowHandler(database);
	}

	public void importDataSet(DataSet dataSet) {
		try {
			if (dataSet == null) {
				log.warn("not an EcoSpold data set");
				return;
			}
			checkImport(dataSet);
		} catch (Exception e) {
			log.error("Failed to import EcoSpold 2 process", e);
		}
	}

	private void checkImport(DataSet dataSet) {
		if (!valid(dataSet)) {
			log.warn("invalid data set -> not imported");
			return;
		}
		Activity activity = dataSet.getActivity();
		try {
			ProcessDao dao = new ProcessDao(database.getEntityFactory());
			boolean contains = dao.getForRefId(activity.getId()) != null;
			if (contains) {
				log.trace("process {} is already in the database",
						activity.getId());
				return;
			}
			log.trace("import process {}", activity.getName());
			runImport(dataSet);
		} catch (Exception e) {
			log.error("Failed to import process", e);
		}
	}

	private boolean valid(DataSet dataSet) {
		Activity activity = dataSet.getActivity();
		if (activity.getId() == null || activity.getName() == null)
			return false;
		IntermediateExchange refFlow = null;
		for (IntermediateExchange techFlow : dataSet.getIntermediateExchanges()) {
			if (techFlow.getOutputGroup() == null)
				continue;
			if (techFlow.getOutputGroup() != 0)
				continue;
			refFlow = techFlow;
			break;
		}
		return refFlow != null;
	}

	private void runImport(DataSet dataSet) throws Exception {
		Activity activity = dataSet.getActivity();
		Process process = new Process();
		process.setRefId(activity.getId());
		process.setName(activity.getName());
		setCategory(process);
		for (IntermediateExchange e : dataSet.getIntermediateExchanges()) {
			if (e.getAmount() == 0)
				continue;
			Flow flow = flowHandler.getFlow(e);
			Exchange exchange = createExchange(e, flow, process);
			if (flow == null)
				continue;
			// TODO: default provider!
			// exchange.setDefaultProviderId(e.getActivityLinkId());
			if (e.getOutputGroup() != null && e.getOutputGroup() == 0)
				process.setQuantitativeReference(exchange);
		}

		for (ElementaryExchange e : dataSet.getElementaryExchanges()) {
			if (e.getAmount() == 0)
				continue;
			Flow flow = flowHandler.getFlow(e);
			createExchange(e, flow, process);
		}
		database.createDao(Process.class).insert(process);
	}

	private Exchange createExchange(org.openlca.ecospold2.Exchange original,
			Flow flow, Process process) {
		if (flow == null || flow.getReferenceFlowProperty() == null) {
			log.warn("invalid exchange {}; not imported", original);
			return null;
		}
		Unit unit = flowHandler.getUnit(original.getUnitId());
		Exchange exchange = new Exchange();
		exchange.setInput(original.getInputGroup() != null);
		exchange.setFlow(flow);
		exchange.setFlowPropertyFactor(flow.getReferenceFactor());
		exchange.setUnit(unit);
		exchange.getResultingAmount().setValue(original.getAmount());
		exchange.getResultingAmount().setFormula(
				Double.toString(original.getAmount()));
		process.getExchanges().add(exchange);
		return exchange;
	}

	// TODO: just for tests
	private void setCategory(Process process) throws Exception {
		String pref = process.getName().substring(0, 1).toLowerCase();
		Category cat = processCategories.get(pref);
		if (cat == null) {
			cat = new Category();
			cat.setModelType(ModelType.PROCESS);
			cat.setName(pref);
			cat.setRefId(UUID.randomUUID().toString());
			BaseDao<Category> dao = database.createDao(Category.class);
			dao.insert(cat);
			processCategories.put(pref, cat);
		}
		process.setCategory(cat);
	}

}