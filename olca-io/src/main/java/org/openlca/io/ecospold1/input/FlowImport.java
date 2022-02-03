package org.openlca.io.ecospold1.input;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IReferenceFunction;
import org.openlca.ecospold.io.DataSet;
import org.openlca.io.UnitMapping;
import org.openlca.io.UnitMappingEntry;
import org.openlca.io.maps.FlowMapEntry;
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns flows from the database or creates them using unit- and
 * flow-mappings. Flows can be imported from EcoSpold 1 exchanges representing
 * process inputs or outputs, exchanges representing impact assessment factors,
 * or reference functions of processes.
 */
class FlowImport {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final DB db;
	private final UnitMapping unitMapping;
	private final Map<String, FlowMapEntry> flowMap;
	private final Map<String, FlowBucket> cache = new HashMap<>();

	public FlowImport(DB db, ImportConfig config) {
		this.db = db;
		this.unitMapping = config.getUnitMapping();
		this.flowMap = config.getFlowMap() == null
			? Collections.emptyMap()
			: config.getFlowMap().index();
	}

	/** Import a flow from a process import or export. */
	public FlowBucket handleProcessExchange(IExchange exchange) {
		String flowKey = ES1KeyGen.forFlow(exchange);
		return handleExchange(flowKey, exchange);
	}

	/** Import a flow from an impact assessment factor. */
	public FlowBucket handleImpactFactor(IExchange exchange) {
		String flowKey = ES1KeyGen.forElementaryFlow(exchange);
		return handleExchange(flowKey, exchange);
	}

	/** Import a flow from an exchange. */
	private FlowBucket handleExchange(String flowKey, IExchange exchange) {
		FlowBucket cached = getCachedOrMapped(flowKey);
		if (cached != null)
			return cached;
		FlowBucket db = getDbFlow(flowKey, exchange);
		if (db != null)
			return cache(flowKey, db);
		Flow flow = new Flow();
		flow.refId = flowKey;
		flow.name = exchange.getName();
		mapExchangeData(exchange, flow);
		String unit = exchange.getUnit();
		FlowBucket created = createFlow(flowKey, flow, unit);
		return cache(flowKey, created);
	}

	/** Import a flow from a reference function. */
	public FlowBucket handleProcessProduct(DataSet dataSet) {
		if (dataSet == null || dataSet.getReferenceFunction() == null)
			return null;
		String flowKey = ES1KeyGen.forProduct(dataSet);
		FlowBucket cached = getCachedOrMapped(flowKey);
		if (cached != null)
			return cached;
		FlowBucket db = getDbFlow(flowKey, dataSet);
		if (db != null)
			return cache(flowKey, db);
		IReferenceFunction refFun = dataSet.getReferenceFunction();
		Flow flow = new Flow();
		flow.refId = flowKey;
		flow.name = refFun.getName();
		mapDataSetData(dataSet, flow);
		String unit = refFun.getUnit();
		FlowBucket created = createFlow(flowKey, flow, unit);
		return cache(flowKey, created);
	}

	/** Get a cached or mapped flow: no unit info is required. */
	private FlowBucket getCachedOrMapped(String flowKey) {
		FlowBucket cached = cache.get(flowKey);
		if (cached != null)
			return cached;
		FlowBucket mapped = getMappedFlow(flowKey);
		if (mapped != null)
			return cache(flowKey, mapped);
		return null;
	}

	/** Cache the bucket for the generated key. */
	private FlowBucket cache(String flowKey, FlowBucket bucket) {
		if (bucket == null || !bucket.isValid()) {
			log.warn("Could not create valid flow {}", flowKey);
			return null;
		}
		cache.put(flowKey, bucket);
		return bucket;
	}

	/** Try to find a flow from the mapping tables. */
	private FlowBucket getMappedFlow(String genKey) {
		var entry = flowMap.get(genKey);
		if (entry == null)
			return null;
		Flow flow = db.get(Flow.class, entry.targetFlowId());
		if (flow == null)
			return null;
		FlowBucket bucket = new FlowBucket();
		bucket.conversionFactor = entry.factor();
		bucket.flow = flow;
		bucket.flowProperty = flow.referenceFlowProperty;
		bucket.unit = getReferenceUnit(bucket.flowProperty);
		if (!bucket.isValid()) {
			log.warn("invalid flow mapping for {}", genKey);
			return null;
		}
		return bucket;
	}

	private Unit getReferenceUnit(FlowProperty flowProperty) {
		if (flowProperty == null)
			return null;
		UnitGroup group = flowProperty.unitGroup;
		if (group == null)
			return null;
		return group.referenceUnit;
	}

	private FlowBucket getDbFlow(String flowKey, IExchange inExchange) {
		Flow flow = db.findFlow(inExchange, flowKey,
				unitMapping.getEntry(inExchange.getUnit()));
		if (flow == null)
			return null;
		return createBucket(flow, inExchange.getUnit());
	}

	private FlowBucket getDbFlow(String flowKey, DataSet dataSet) {
		if (dataSet.getReferenceFunction() == null)
			return null;
		IReferenceFunction refFun = dataSet.getReferenceFunction();
		Flow flow = db.findFlow(dataSet, flowKey,
				unitMapping.getEntry(refFun.getUnit()));
		if (flow == null)
			return null;
		return createBucket(flow, refFun.getUnit());
	}

	private void mapExchangeData(IExchange inExchange, Flow flow) {
		flow.casNumber = inExchange.getCASNumber();
		flow.formula = inExchange.getFormula();
		if (inExchange.isInfrastructureProcess() != null)
			flow.infrastructureFlow = inExchange.isInfrastructureProcess();
		Category flowCategory = db.getPutCategory(ModelType.FLOW,
				inExchange.getCategory(), inExchange.getSubCategory());
		if (flowCategory != null)
			flow.category = flowCategory;
		String locationCode = inExchange.getLocation();
		if (locationCode != null) {
			String locKey = KeyGen.get(locationCode);
			flow.location = db.findLocation(locationCode, locKey);
		}
		flow.flowType = Mapper.getFlowType(inExchange);
	}

	private void mapDataSetData(DataSet dataset, Flow flow) {
		IReferenceFunction refFun = dataset.getReferenceFunction();
		flow.casNumber = refFun.getCASNumber();
		flow.formula = refFun.getFormula();
		Category flowCategory = db.getPutCategory(ModelType.FLOW,
				refFun.getCategory(), refFun.getSubCategory());
		if (flowCategory != null)
			flow.category = flowCategory;
		if (dataset.getGeography() != null
				&& dataset.getGeography().getLocation() != null) {
			String code = dataset.getGeography().getLocation();
			String locKey = KeyGen.get(code);
			flow.location = db.findLocation(code, locKey);
		}
		flow.flowType = FlowType.PRODUCT_FLOW;
	}

	/** Creates a new flow and inserts it in the database. */
	private FlowBucket createFlow(String flowKey, Flow flow, String unit) {
		var entry = unitMapping.getEntry(unit);
		if (entry == null || !entry.isValid()) {
			// we have no valid unit mapping; we create
			// a new one here
			var _db = db.database;
			var units = _db.insert(UnitGroup.of("New: " + unit, unit));
			var prop = _db.insert(FlowProperty.of("New: " + unit, units));
			entry = new UnitMappingEntry();
			entry.factor = 1.0;
			entry.flowProperty = prop;
			entry.unit = units.referenceUnit;
			entry.unitGroup = units;
			entry.unitName = unit;
			unitMapping.put(unit, entry);
		}
		flow.referenceFlowProperty = entry.flowProperty;
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.flowProperty = entry.flowProperty;
		factor.conversionFactor = 1.0;
		flow.flowPropertyFactors.add(factor);
		db.put(flow, flowKey);
		return createBucket(flow, unit);
	}

	/** Create the flow bucket for the given flow and unit. */
	private FlowBucket createBucket(Flow flow, String unit) {
		UnitMappingEntry mapEntry = unitMapping.getEntry(unit);
		if (mapEntry == null || !mapEntry.isValid())
			return null;
		if (flow.getFactor(mapEntry.flowProperty) == null) {
			log.error("The unit/property for flow {}/{} "
					+ "changed in the database", flow, unit);
			return null;
		}
		FlowBucket bucket = new FlowBucket();
		bucket.conversionFactor = 1.0;
		bucket.flow = flow;
		bucket.flowProperty = mapEntry.flowProperty;
		bucket.unit = mapEntry.unit;
		return bucket;
	}

}
