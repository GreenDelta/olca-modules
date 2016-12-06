package org.openlca.io.ecospold2.input;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.openlca.core.database.BaseEntityDao;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.ecospold2.Classification;
import org.openlca.ecospold2.Compartment;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.ElementaryExchange;
import org.openlca.ecospold2.Exchange;
import org.openlca.ecospold2.Geography;
import org.openlca.ecospold2.IntermediateExchange;
import org.openlca.io.Categories;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowMapEntry;
import org.openlca.io.maps.MapType;
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imports the reference data from a set of EcoSpold 02 files. During the import
 * it creates an index that then can be used in a real process import.
 */
class RefDataImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final ImportConfig config;
	private IDatabase database;
	private CategoryDao categoryDao;
	private FlowDao flowDao;
	private BaseEntityDao<Location> locationDao;
	private RefDataIndex index;
	private FlowMap flowMap;

	public RefDataImport(IDatabase database, ImportConfig config) {
		this.config = config;
		this.database = database;
		this.index = new RefDataIndex();
		this.categoryDao = new CategoryDao(database);
		this.locationDao = new BaseEntityDao<>(Location.class, database);
		this.flowDao = new FlowDao(database);
		this.flowMap = new FlowMap(MapType.ES2_FLOW);
		try {
			loadUnitMaps(database);
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
			BaseEntityDao<Unit> unitDao = new BaseEntityDao<>(Unit.class,
					database);
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

	public void importDataSet(DataSet dataSet) {
		if (dataSet == null)
			return;
		try {
			classification(dataSet);
			geography(dataSet);
			for (IntermediateExchange e : dataSet.intermediateExchanges) {
				if (e.amount == 0 && config.skipNullExchanges)
					continue;
				productFlow(dataSet, e);
			}
			for (ElementaryExchange e : dataSet.elementaryExchanges) {
				elementaryFlow(e);
			}
		} catch (Exception e) {
			log.error("failed to import reference data from data set", e);
		}
	}

	private void classification(DataSet dataSet) {
		Classification classification = findClassification(dataSet);
		if (classification == null || classification.classificationId == null)
			return;
		String refId = classification.classificationId;
		Category category = index.getProcessCategory(refId);
		if (category != null)
			return;
		category = categoryDao.getForRefId(refId);
		if (category == null) {
			category = new Category();
			category.setDescription(classification.classificationSystem);
			category.setModelType(ModelType.PROCESS);
			category.setName(classification.classificationValue);
			category.setRefId(refId);
			category = categoryDao.insert(category);
		}
		index.putProcessCategory(refId, category);
	}

	private Classification findClassification(DataSet dataSet) {
		for (Classification classification : dataSet.classifications) {
			if (classification.classificationSystem == null)
				continue;
			if (classification.classificationSystem.startsWith("ISIC"))
				return classification;
		}
		return null;
	}

	private void geography(DataSet dataSet) {
		Geography geography = dataSet.geography;
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
		if (compartment == null || compartment.subcompartmentId == null
				|| compartment.subcompartment == null
				|| compartment.compartment == null)
			return;
		String refId = compartment.subcompartmentId;
		Category category = index.getCompartment(refId);
		if (category != null)
			return;
		category = categoryDao.getForRefId(refId);
		if (category == null) {
			Category parent = Categories.findOrCreateRoot(database,
					ModelType.FLOW, compartment.compartment);
			category = Categories.findOrAddChild(database, parent,
					compartment.subcompartment);
		}
		index.putCompartment(refId, category);
	}

	private void productFlow(DataSet dataSet, IntermediateExchange exchange) {
		String refId = exchange.intermediateExchangeId;
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
				+ exchange.intermediateExchangeId);
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
		String refId = exchange.elementaryExchangeId;
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
			category = index.getCompartment(exchange.compartment.subcompartmentId);
		}
		flow = new Flow();
		flow.setRefId(refId);
		flow.setCategory(category);
		flow.setDescription("EcoSpold 2 elementary exchange, ID = "
				+ exchange.elementaryExchangeId);
		flow.setFlowType(FlowType.ELEMENTARY_FLOW);
		createFlow(exchange, flow);
	}

	/**
	 * Tries to load an elementary flow from the database, which could also be a
	 * mapped flow.
	 */
	private Flow loadElemDBFlow(ElementaryExchange exchange) {
		String extId = exchange.elementaryExchangeId;
		Flow flow = flowDao.getForRefId(extId);
		if (flow != null)
			return flow;
		FlowMapEntry entry = flowMap.getEntry(extId);
		if (entry == null)
			return null;
		flow = flowDao.getForRefId(entry.getOpenlcaFlowKey());
		if (flow == null)
			return null;
		index.putMappedFlow(extId, entry.getConversionFactor());
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
	 * Returns only a value if the given exchange is the reference product of
	 * the data set.
	 */
	private Category getProductCategory(DataSet dataSet, IntermediateExchange e) {
		Integer og = e.outputGroup;
		if (og == null || og != 0)
			return null;
		Classification clazz = findClassification(dataSet);
		if (clazz == null || clazz.classificationValue == null)
			return null;
		Category cat = Categories.findOrCreateRoot(database, ModelType.FLOW,
				clazz.classificationValue);
		return cat;
	}
}
