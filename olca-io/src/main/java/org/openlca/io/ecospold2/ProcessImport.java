package org.openlca.io.ecospold2;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.openlca.core.database.BaseDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;
	private FlowHandler flowHandler;
	private SAXBuilder builder;
	private Map<String, Category> processCategories = new HashMap<>();

	public ProcessImport(IDatabase database) {
		this.database = database;
		this.flowHandler = new FlowHandler(database);
		builder = new SAXBuilder();
	}

	public void importProcess(InputStream stream) {
		try {
			Document doc = builder.build(stream);
			Element dataSetElement = getDataSetElement(doc);
			if (dataSetElement == null) {
				log.warn("not an EcoSpold data set");
				return;
			}
			runImport(dataSetElement);
		} catch (Exception e) {
			log.error("Failed to import EcoSpold 2 process", e);
		}
	}

	private Element getDataSetElement(Document doc) {
		if (doc == null)
			return null;
		Element root = doc.getRootElement();
		if (!"ecoSpold".equals(root.getName()))
			return null;
		Element e = root.getChild("activityDataset", root.getNamespace());
		if (e == null)
			e = root.getChild("childActivityDataset", root.getNamespace());
		return e;
	}

	private void runImport(Element dataSet) {
		LeanProcess leanProcess = LeanProcess.create(dataSet);
		if (!valid(leanProcess)) {
			log.warn("invalid data set {} {}", leanProcess.getId(),
					leanProcess.getName());
			return;
		}
		try {
			boolean contains = database.createDao(Process.class).contains(
					leanProcess.getId());
			if (contains) {
				log.trace("process {} is already in the database",
						leanProcess.getId());
				return;
			}
			log.trace("import process {}", leanProcess.getName());
			runImport(leanProcess);
		} catch (Exception e) {
			log.error("Failed to import process", e);
		}
	}

	private boolean valid(LeanProcess leanProcess) {
		if (leanProcess.getId() == null || leanProcess.getName() == null)
			return false;
		LeanExchange refFlow = null;
		for (LeanExchange techFlow : leanProcess.getExchanges()) {
			if (techFlow.getOutputGroup() == null)
				continue;
			if (techFlow.getOutputGroup() != 0)
				continue;
			refFlow = techFlow;
			break;
		}
		return refFlow != null;
	}

	private void runImport(LeanProcess leanProcess) throws Exception {
		Process process = new Process(leanProcess.getId(),
				leanProcess.getName());
		setCategory(process);
		for (LeanExchange e : leanProcess.getExchanges()) {
			if (e.getAmount() == 0)
				continue;
			Flow flow = flowHandler.getFlow(e);
			Unit unit = flowHandler.getUnit(e.getUnitId());
			if (flow == null || unit == null) {
				log.warn("could not create exchange");
				continue;
			}
			Exchange exchange = new Exchange();
			exchange.setId(UUID.randomUUID().toString());
			exchange.setInput(e.getInputGroup() != null);
			exchange.setDefaultProviderId(e.getActivityLinkId());
			exchange.setFlow(flow);
			exchange.setFlowPropertyFactor(flow.getReferencePropertyFactor());
			exchange.setUnit(unit);
			exchange.getResultingAmount().setValue(e.getAmount());
			exchange.getResultingAmount().setFormula(
					Double.toString(e.getAmount()));
			if (e.getOutputGroup() != null && e.getOutputGroup() == 0)
				process.setQuantitativeReference(exchange);
			process.add(exchange);
		}
		database.createDao(Process.class).insert(process);
	}

	// TODO: just for tests
	private void setCategory(Process process) throws Exception {
		String pref = process.getName().substring(0, 1).toLowerCase();
		Category cat = processCategories.get(pref);
		if (cat == null) {
			cat = new Category(UUID.randomUUID().toString(), pref,
					Process.class.getCanonicalName());
			BaseDao<Category> dao = database.createDao(Category.class);
			Category parent = dao.getForId(Process.class.getCanonicalName());
			parent.add(cat);
			cat.setParentCategory(parent);
			dao.update(parent);
			processCategories.put(pref, cat);
		}
		process.setCategoryId(cat.getId());
	}

}
