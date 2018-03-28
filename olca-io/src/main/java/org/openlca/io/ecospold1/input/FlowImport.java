package org.openlca.io.ecospold1.input;

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
import org.openlca.io.maps.FlowMap;
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

	private Logger log = LoggerFactory.getLogger(getClass());
	private DB db;
	private UnitMapping unitMapping;
	private FlowMap flowMap;
	private Map<String, FlowBucket> cache = new HashMap<>();

	public FlowImport(DB db, ImportConfig config) {
		this.db = db;
		this.unitMapping = config.getUnitMapping();
		this.flowMap = config.getFlowMap();
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
		flow.setRefId(flowKey);
		flow.setName(exchange.getName());
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
		flow.setRefId(flowKey);
		flow.setName(refFun.getName());
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
		FlowMapEntry entry = flowMap.getEntry(genKey);
		if (entry == null)
			return null;
		Flow flow = db.get(Flow.class, entry.referenceFlowID);
		if (flow == null)
			return null;
		FlowBucket bucket = new FlowBucket();
		bucket.conversionFactor = entry.conversionFactor;
		bucket.flow = flow;
		bucket.flowProperty = flow.getReferenceFlowProperty();
		Unit unit = getReferenceUnit(bucket.flowProperty);
		bucket.unit = unit;
		if (!bucket.isValid()) {
			log.warn("invalid flow mapping for {}", genKey);
			return null;
		}
		return bucket;
	}

	private Unit getReferenceUnit(FlowProperty flowProperty) {
		if (flowProperty == null)
			return null;
		UnitGroup group = flowProperty.getUnitGroup();
		if (group == null)
			return null;
		return group.getReferenceUnit();
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
		flow.setCasNumber(inExchange.getCASNumber());
		flow.setFormula(inExchange.getFormula());
		if (inExchange.isInfrastructureProcess() != null)
			flow.setInfrastructureFlow(inExchange.isInfrastructureProcess());
		Category flowCategory = db.getPutCategory(ModelType.FLOW,
				inExchange.getCategory(), inExchange.getSubCategory());
		if (flowCategory != null)
			flow.setCategory(flowCategory);
		String locationCode = inExchange.getLocation();
		if (locationCode != null) {
			String locKey = KeyGen.get(locationCode);
			flow.setLocation(db.findLocation(locationCode, locKey));
		}
		FlowType flowType = Mapper.getFlowType(inExchange);
		flow.setFlowType(flowType);
	}

	private void mapDataSetData(DataSet dataset, Flow flow) {
		IReferenceFunction refFun = dataset.getReferenceFunction();
		flow.setCasNumber(refFun.getCASNumber());
		flow.setFormula(refFun.getFormula());
		Category flowCategory = db.getPutCategory(ModelType.FLOW,
				refFun.getCategory(), refFun.getSubCategory());
		if (flowCategory != null)
			flow.setCategory(flowCategory);
		if (dataset.getGeography() != null
				&& dataset.getGeography().getLocation() != null) {
			String code = dataset.getGeography().getLocation();
			String locKey = KeyGen.get(code);
			flow.setLocation(db.findLocation(code, locKey));
		}
		flow.setFlowType(FlowType.PRODUCT_FLOW);
	}

	/** Creates a new flow and inserts it in the database. */
	private FlowBucket createFlow(String flowKey, Flow flow, String unit) {
		UnitMappingEntry entry = unitMapping.getEntry(unit);
		if (entry == null || !entry.isValid())
			return null;
		flow.setReferenceFlowProperty(entry.flowProperty);
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setFlowProperty(entry.flowProperty);
		factor.setConversionFactor(1.0);
		flow.getFlowPropertyFactors().add(factor);
		db.put(flow, flowKey);
		return createBucket(flow, unit);
	}

	/** Create the flow bucket for the given flow and unit. */
	private FlowBucket createBucket(Flow flow, String unit) {
		UnitMappingEntry mapEntry = unitMapping.getEntry(unit);
		if (mapEntry == null || !mapEntry.isValid())
			return null;
		if (flow.getFactor(mapEntry.flowProperty) != null) {
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
