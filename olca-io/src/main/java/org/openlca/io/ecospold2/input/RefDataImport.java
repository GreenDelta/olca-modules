package org.openlca.io.ecospold2.input;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.UnitDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.io.Categories;
import org.openlca.io.maps.FlowMapEntry;
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spold2.Classification;
import spold2.Compartment;
import spold2.DataSet;
import spold2.ElementaryExchange;
import spold2.Exchange;
import spold2.Geography;
import spold2.IntermediateExchange;
import spold2.Spold2;

/**
 * Imports the reference data from a set of EcoSpold 02 files. During the import
 * it creates an index that then can be used in a real process import.
 */
class RefDataImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final ImportConfig config;
	private CategoryDao categoryDao;
	private FlowDao flowDao;
	private LocationDao locationDao;
	private RefDataIndex index;

	public RefDataImport(ImportConfig config) {
		this.config = config;
		this.index = new RefDataIndex();
		this.categoryDao = new CategoryDao(config.db);
		this.locationDao = new LocationDao(config.db);
		this.flowDao = new FlowDao(config.db);
		try {
			loadUnitMaps(config.db);
		} catch (Exception e) {
			log.error("failed to load unit map", e);
		}
	}

	public RefDataIndex getIndex() {
		return index;
	}

	private void loadUnitMaps(IDatabase database) throws Exception {
		InputStream is = getClass().getResourceAsStream("ei3_unit_map.csv");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] args = line.split(",");
			String eiUnitKey = args[0];
			UnitDao unitDao = new UnitDao(database);
			Unit unit = unitDao.getForRefId(args[1]);
			FlowPropertyDao propDao = new FlowPropertyDao(database);
			FlowProperty prop = propDao.getForRefId(args[2]);
			if (unit == null || prop == null)
				log.warn("no unit or property found for {} in database, "
						+ "no reference data?", eiUnitKey);
			else {
				index.putUnit(eiUnitKey, unit);
				index.putFlowProperty(eiUnitKey, prop);
			}
		}
	}

	public void importDataSet(DataSet ds) {
		if (ds == null)
			return;
		try {
			classification(ds);
			geography(ds);
			for (IntermediateExchange e : Spold2.getProducts(ds)) {
				if (e.amount == 0 && config.skipNullExchanges)
					continue;
				productFlow(ds, e);
			}
			for (ElementaryExchange e : Spold2.getElemFlows(ds)) {
				elementaryFlow(e);
			}
		} catch (Exception e) {
			log.error("failed to import reference data from data set", e);
		}
	}

	private void classification(DataSet dataSet) {
		Classification classification = findClassification(dataSet);
		if (classification == null || classification.id == null)
			return;
		String refId = classification.id;
		Category category = index.getProcessCategory(refId);
		if (category != null)
			return;
		category = categoryDao.getForRefId(refId);
		if (category == null) {
			category = new Category();
			category.setDescription(classification.system);
			category.setModelType(ModelType.PROCESS);
			category.setName(classification.value);
			category.setRefId(refId);
			category = categoryDao.insert(category);
		}
		index.putProcessCategory(refId, category);
	}

	private Classification findClassification(DataSet ds) {
		for (Classification c : Spold2.getClassifications(ds)) {
			if (c.system == null)
				continue;
			if (c.system.startsWith("ISIC"))
				return c;
		}
		return null;
	}

	private void geography(DataSet ds) {
		Geography geography = Spold2.getGeography(ds);
		if (geography == null || geography.id == null
				|| geography.shortName == null)
			return;
		String refId = geography.id;
		Location location = index.getLocation(refId);
		if (location != null)
			return;
		String genKey = KeyGen.get(geography.shortName);
		location = locationDao.getForRefId(genKey);
		if (location == null) {
			location = new Location();
			location.setCode(geography.shortName);
			location.setName(geography.shortName);
			location.setDescription("imported via EcoSpold 02 import");
			location.setRefId(genKey);
			location = locationDao.insert(location);
		}
		index.putLocation(refId, location);
	}

	private void compartment(Compartment compartment) {
		if (compartment == null || compartment.id == null
				|| compartment.subCompartment == null
				|| compartment.compartment == null)
			return;
		String refId = compartment.id;
		Category category = index.getCompartment(refId);
		if (category != null)
			return;
		category = categoryDao.getForRefId(refId);
		if (category == null) {
			Category parent = Categories.findOrCreateRoot(config.db,
					ModelType.FLOW, compartment.compartment);
			category = Categories.findOrAddChild(config.db, parent,
					compartment.subCompartment);
		}
		index.putCompartment(refId, category);
	}

	private void productFlow(DataSet dataSet, IntermediateExchange exchange) {
		String refId = exchange.flowId;
		Flow flow = index.getFlow(refId);
		if (flow == null) {
			flow = flowDao.getForRefId(refId);
			if (flow != null)
				index.putFlow(refId, flow);
		}
		if (flow == null)
			flow = createNewProduct(exchange, refId);
		Integer og = exchange.outputGroup;
		boolean isRef = og != null && og == 0;
		if (!isRef)
			return;
		index.putNegativeFlow(refId, exchange.amount < 0);
		Category category = getProductCategory(dataSet, exchange);
		flow.setCategory(category);
		flow = flowDao.update(flow);
		index.putFlow(refId, flow);
	}

	private Flow createNewProduct(IntermediateExchange exchange, String refId) {
		Flow flow;
		flow = new Flow();
		flow.setRefId(refId);
		flow.setDescription("EcoSpold 2 intermediate exchange, ID = "
				+ exchange.flowId);
		// in ecoinvent 3 negative values indicate waste flows
		// see also the exchange handling in the process input
		// to be on the save side, we declare all intermediate flows as
		// products
		// FlowType type = exchange.getAmount() < 0 ? FlowType.WASTE_FLOW
		// : FlowType.PRODUCT_FLOW;
		flow.setFlowType(FlowType.PRODUCT_FLOW);
		createFlow(exchange, flow);
		return flow;
	}

	private void elementaryFlow(ElementaryExchange exchange) {
		String refId = exchange.flowId;
		Flow flow = index.getFlow(refId);
		if (flow != null)
			return;
		flow = loadElemDBFlow(exchange);
		if (flow != null) {
			index.putFlow(refId, flow);
			return;
		}
		Category category = null;
		if (exchange.compartment != null) {
			compartment(exchange.compartment);
			category = index.getCompartment(exchange.compartment.id);
		}
		flow = new Flow();
		flow.setRefId(refId);
		flow.setCategory(category);
		flow.setDescription("EcoSpold 2 elementary exchange, ID = "
				+ exchange.flowId);
		flow.setFlowType(FlowType.ELEMENTARY_FLOW);
		createFlow(exchange, flow);
	}

	/**
	 * Tries to load an elementary flow from the database, which could also be a
	 * mapped flow.
	 */
	private Flow loadElemDBFlow(ElementaryExchange exchange) {
		String extId = exchange.flowId;
		Flow flow = flowDao.getForRefId(extId);
		if (flow != null)
			return flow;
		FlowMapEntry entry = config.getFlowMap().getEntry(extId);
		if (entry == null)
			return null;
		flow = flowDao.getForRefId(entry.referenceFlowID);
		if (flow == null)
			return null;
		index.putMappedFlow(extId, entry.conversionFactor);
		return flow;
	}

	private void createFlow(Exchange exchange, Flow flow) {
		flow.setName(exchange.name);
		FlowProperty prop = index.getFlowProperty(exchange.unitId);
		if (prop == null) {
			log.warn("unknown unit {}", exchange.unitId);
			return;
		}
		FlowPropertyFactor fac = new FlowPropertyFactor();
		fac.setFlowProperty(prop);
		fac.setConversionFactor(1.0);
		flow.getFlowPropertyFactors().add(fac);
		flow.setReferenceFlowProperty(prop);
		try {
			flow = flowDao.insert(flow);
			index.putFlow(flow.getRefId(), flow);
		} catch (Exception e) {
			log.error("Failed to store flow", e);
		}
	}

	/**
	 * Returns only a value if the given exchange is the reference product of the
	 * data set.
	 */
	private Category getProductCategory(DataSet dataSet,
			IntermediateExchange e) {
		Integer og = e.outputGroup;
		if (og == null || og != 0)
			return null;
		Classification clazz = findClassification(dataSet);
		if (clazz == null || clazz.value == null)
			return null;
		Category cat = Categories.findOrCreateRoot(config.db, ModelType.FLOW,
				clazz.value);
		return cat;
	}
}
