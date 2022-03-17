package org.openlca.io.ecospold2.input;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Objects;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.UnitDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.io.Categories;
import org.openlca.io.maps.FlowMapEntry;
import org.openlca.util.KeyGen;

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


	private final ImportConfig config;
	private final ImportLog log;
	private final CategoryDao categoryDao;
	private final FlowDao flowDao;
	private final LocationDao locationDao;
	private final RefDataIndex index;

	public RefDataImport(ImportConfig config) {
		this.config = config;
		this.log = config.log();
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
		var is = getClass().getResourceAsStream("ei3_unit_map.csv");
		if (is == null)
			return;
		try (var reader = new BufferedReader(new InputStreamReader(is))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] args = line.split(",");
				String eiUnitKey = args[0];
				UnitDao unitDao = new UnitDao(database);
				Unit unit = unitDao.getForRefId(args[1]);
				FlowPropertyDao propDao = new FlowPropertyDao(database);
				FlowProperty prop = propDao.getForRefId(args[2]);
				if (unit == null || prop == null)
					log.warn("no unit or property found for '" +
						eiUnitKey + "' in database, no reference data?");
				else {
					index.putUnit(eiUnitKey, unit);
					index.putFlowProperty(eiUnitKey, prop);
				}
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

	private void classification(DataSet ds) {
		Classification classification = findClassification(ds);
		if (classification == null || classification.id == null)
			return;
		String refId = classification.id;
		Category c = index.getProcessCategory(refId);
		if (c != null)
			return;
		c = categoryDao.getForRefId(refId);
		if (c == null) {
			c = new Category();
			c.description = classification.system;
			c.modelType = ModelType.PROCESS;
			c.name = classification.value;
			c.refId = refId;
			c = categoryDao.insert(c);
		}
		index.putProcessCategory(refId, c);
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
			location.code = geography.shortName;
			location.name = geography.shortName;
			location.description = "imported via EcoSpold 02 import";
			location.refId = genKey;
			location = locationDao.insert(location);
		}
		index.putLocation(refId, location);
	}

	private void compartment(Compartment comp) {
		if (comp == null || comp.id == null
			|| comp.subCompartment == null
			|| comp.compartment == null)
			return;
		String refId = comp.id;
		Category category = index.getCompartment(refId);
		if (category != null)
			return;
		category = categoryDao.sync(
			ModelType.FLOW, comp.compartment, comp.subCompartment);
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
		if (flow == null) {
			flow = createNewProduct(exchange, refId);
		}
		Integer og = exchange.outputGroup;
		boolean isRef = og != null && og == 0;
		if (!isRef)
			return;
		flow.category = getProductCategory(dataSet, exchange);
		flow = flowDao.update(flow);
		index.putFlow(refId, flow);
	}

	private Flow createNewProduct(IntermediateExchange exchange, String refId) {
		Flow flow;
		flow = new Flow();
		flow.refId = refId;
		flow.description = "EcoSpold 2 intermediate exchange, ID = "
			+ exchange.flowId;
		// in ecoinvent 3 negative values indicate waste flows
		// see also the exchange handling in the process input
		// to be on the save side, we declare all intermediate flows as
		// products
		// FlowType type = exchange.getAmount() < 0 ? FlowType.WASTE_FLOW
		// : FlowType.PRODUCT_FLOW;
		flow.flowType = FlowType.PRODUCT_FLOW;
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
		flow.refId = refId;
		flow.category = category;
		flow.description = "EcoSpold 2 elementary exchange, ID = "
			+ exchange.flowId;
		flow.flowType = FlowType.ELEMENTARY_FLOW;
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
		FlowMapEntry entry = config.getFlowMap().get(extId);
		if (entry == null)
			return null;
		flow = flowDao.getForRefId(entry.targetFlowId());
		if (flow == null)
			return null;
		index.putMappedFlow(extId, entry.factor());
		return flow;
	}

	private void createFlow(Exchange exchange, Flow flow) {
		flow.name = exchange.name;
		FlowProperty prop = index.getFlowProperty(exchange.unitId);
		if (prop == null) {
			prop = syncUnit(exchange.unitId, exchange.unit);
			if (prop == null) {
				log.warn("failed to create unit: " + exchange.unit);
				return;
			}
		}
		FlowPropertyFactor fac = new FlowPropertyFactor();
		fac.flowProperty = prop;
		fac.conversionFactor = 1.0;
		flow.flowPropertyFactors.add(fac);
		flow.referenceFlowProperty = prop;
		try {
			flow = flowDao.insert(flow);
			index.putFlow(flow.refId, flow);
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
		if (clazz == null || clazz.value == null)
			return null;
		return Categories.findOrCreateRoot(config.db, ModelType.FLOW, clazz.value);
	}

	/**
	 * For units that are not found in the mapping file, we try to find the
	 * corresponding unit and flow property pair from the database. First, we
	 * check if there is a unit with the given ID defined. If this is not the
	 * case we search for a unit with the given name (or synonym). When we find
	 * such a unit, we try to find also the default flow property and when there
	 * is no such flow property some other flow property with that unit in the
	 * corresponding unit group. If we cannot find something we create the flow
	 * property, unit group, and unit if necessary.
	 */
	private FlowProperty syncUnit(String refID, String name) {

		// search for the unit
		Unit unit = null;
		UnitGroup group = null;
		boolean byID = false;
		for (UnitGroup ug : new UnitGroupDao(config.db).getAll()) {
			for (Unit u : ug.units) {
				if (Objects.equals(u.refId, refID)) {
					unit = u;
					group = ug;
					byID = true;
					break;
				}
				if (Objects.equals(u.name, name)) {
					if (unit != null) {
						log.warn("There are multiple possible" +
							" definitions for unit " + name + " in the database");
					}
					unit = u;
					group = ug;
				}
			}
			if (byID) {
				break;
			}
		}

		// create the unit and unit group if necessary
		if (unit != null) {
			log.info("mapped unit '" + name + "' id='" + refID + "' by "
				+ (byID ? "ID" : "name"));
		} else {
			log.info("create new unit: " + name);

			unit = new Unit();
			unit.name = name;
			unit.refId = refID;
			unit.conversionFactor = 1.0;

			group = new UnitGroup();
			group.name = "Unit group for " + name;
			group.refId = KeyGen.get(ModelType.UNIT_GROUP.name(), refID);
			group.referenceUnit = unit;
			group.units.add(unit);
			group.lastChange = new Date().getTime();
			group.version = Version.valueOf(1, 0, 0);
			group = new UnitGroupDao(config.db).insert(group);
			unit = group.referenceUnit; // JPA synced
			log.imported(group);
		}

		// try to find a matching flow property
		FlowProperty prop = group.defaultFlowProperty;
		FlowPropertyDao propDao = new FlowPropertyDao(config.db);
		if (prop == null) {
			for (FlowProperty fp : propDao.getAll()) {
				if (Objects.equals(fp.unitGroup, group)) {
					prop = fp;
					break;
				}
			}
		}

		// create a new flow property if this is necessary
		if (prop == null) {
			prop = new FlowProperty();
			prop.name = "Flow property for " + name;
			prop.refId = KeyGen.get(ModelType.FLOW_PROPERTY.name(), refID);
			prop.unitGroup = group;
			prop.flowPropertyType = FlowPropertyType.PHYSICAL;
			prop.lastChange = new Date().getTime();
			prop.version = Version.valueOf(1, 0, 0);
			prop = propDao.insert(prop);
			log.imported(prop);
			group.defaultFlowProperty = prop;
			group = new UnitGroupDao(config.db).update(group);
			unit = group.referenceUnit; // JPA synced
		}

		index.putFlowProperty(refID, prop);
		index.putUnit(refID, unit);
		return prop;
	}
}
