package org.openlca.io.ecospold2;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openlca.core.database.BaseDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.ecospold2.ElementaryExchange;
import org.openlca.ecospold2.Exchange;
import org.openlca.ecospold2.IntermediateExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowHandler {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;
	private Map<String, Flow> cachedFlows = new HashMap<>();
	private Map<String, Unit> unitMap = new HashMap<>();
	private Map<String, FlowProperty> propertyMap = new HashMap<>();
	private Map<String, Category> flowCategories = new HashMap<>();

	public FlowHandler(IDatabase database) {
		this.database = database;
	}

	public Unit getUnit(String id) {
		return unitMap.get(id);
	}

	public Flow getFlow(IntermediateExchange exchange) {
		Flow dbFlow = getDbFlow(exchange.getIntermediateExchangeId());
		if (dbFlow != null)
			return dbFlow;
		return createProduct(exchange);
	}

	public Flow getFlow(ElementaryExchange exchange) {
		Flow dbFlow = getDbFlow(exchange.getElementaryExchangeId());
		if (dbFlow != null)
			return dbFlow;
		return createElemFlow(exchange);
	}

	private Flow getDbFlow(String flowId) {
		if (flowId == null)
			return null;
		Flow flow = cachedFlows.get(flowId);
		if (flow != null)
			return flow;
		return getFromDb(flowId);
	}

	private Flow getFromDb(String flowId) {
		try {
			FlowDao dao = new FlowDao(database.getEntityFactory());
			Flow flow = dao.getForRefId(flowId);
			cachedFlows.put(flowId, flow);
			return flow;
		} catch (Exception e) {
			log.error("Failed to load flow from DB, id=" + flowId, e);
			return null;
		}
	}

	private Flow createProduct(IntermediateExchange exchange) {
		Flow flow = new Flow();
		flow.setRefId(exchange.getIntermediateExchangeId());
		flow.setFlowType(FlowType.PRODUCT_FLOW);
		fillCacheFlow(exchange, flow);
		return flow;
	}

	private Flow createElemFlow(ElementaryExchange exchange) {
		Flow flow = new Flow();
		flow.setRefId(exchange.getElementaryExchangeId());
		flow.setFlowType(FlowType.ELEMENTARY_FLOW);
		fillCacheFlow(exchange, flow);
		return flow;
	}

	private void fillCacheFlow(Exchange exchange, Flow flow) {
		flow.setName(exchange.getName());
		FlowProperty prop = propertyMap.get(exchange.getUnitId());
		if (prop == null) {
			log.warn("unknown unit {}", exchange.getUnitId());
			return;
		}
		FlowPropertyFactor fac = new FlowPropertyFactor();
		fac.setFlowProperty(prop);
		fac.setConversionFactor(1.0);
		flow.getFlowPropertyFactors().add(fac);
		flow.setReferenceFlowProperty(prop);
		try {
			setCategory(flow);
			database.createDao(Flow.class).insert(flow);
			cachedFlows.put(flow.getRefId(), flow);
		} catch (Exception e) {
			log.error("Failed to store flow", e);
		}
	}

	private void setCategory(Flow flow) throws Exception {
		String pref = flow.getName().substring(0, 1).toLowerCase();
		Category cat = flowCategories.get(pref);
		if (cat == null) {
			cat = new Category();
			cat.setRefId(UUID.randomUUID().toString());
			cat.setName(pref);
			cat.setModelType(ModelType.FLOW);
			BaseDao<Category> dao = database.createDao(Category.class);
			dao.insert(cat);
			flowCategories.put(pref, cat);
		}
		flow.setCategory(cat);
	}

}