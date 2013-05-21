package org.openlca.io.ecospold2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openlca.core.database.BaseDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Unit;
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
		try {
			loadMaps();
		} catch (Exception e) {
			log.error("Failed to load unit - maps", e);
		}
	}

	private void loadMaps() throws Exception {
		InputStream is = getClass().getResourceAsStream("ei3_unit_map.csv");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] args = line.split(",");
			String eiUnit = args[0];
			Unit unit = database.createDao(Unit.class).getForId(args[1]);
			FlowProperty prop = database.createDao(FlowProperty.class)
					.getForId(args[2]);
			if (unit != null && prop != null) {
				unitMap.put(eiUnit, unit);
				propertyMap.put(eiUnit, prop);
			}
		}
	}

	public Unit getUnit(String id) {
		return unitMap.get(id);
	}

	public Flow getFlow(LeanExchange exchange) {
		String flowId = exchange.getFlowId();
		if (flowId == null)
			return null;
		Flow flow = cachedFlows.get(flowId);
		if (flow != null)
			return flow;
		flow = getFromDb(flowId);
		if (flow != null)
			return flow;
		return createNew(exchange);
	}

	private Flow getFromDb(String flowId) {
		try {
			Flow flow = database.createDao(Flow.class).getForId(flowId);
			cachedFlows.put(flowId, flow);
			return flow;
		} catch (Exception e) {
			log.error("Failed to load flow from DB, id=" + flowId, e);
			return null;
		}
	}

	private Flow createNew(LeanExchange exchange) {
		Flow flow = new Flow(exchange.getFlowId(), exchange.getName());
		FlowProperty prop = propertyMap.get(exchange.getUnitId());
		if (prop == null) {
			log.warn("unknown unit {}, could not create flow {}",
					exchange.getUnitId(), exchange.getFlowId());
			return null;
		}
		FlowPropertyFactor fac = new FlowPropertyFactor(UUID.randomUUID()
				.toString(), prop, 1.0);
		flow.add(fac);
		flow.setReferenceFlowProperty(prop);
		FlowType type = exchange.getType() == LeanExchange.ELEMENTARY_FLOW ? FlowType.ElementaryFlow
				: FlowType.ProductFlow;
		flow.setFlowType(type);
		try {
			setCategory(flow);
			database.createDao(Flow.class).insert(flow);
			cachedFlows.put(flow.getId(), flow);
			return flow;
		} catch (Exception e) {
			log.error("Failed to store flow", e);
			return null;
		}
	}

	private void setCategory(Flow flow) throws Exception {
		String pref = flow.getName().substring(0, 1).toLowerCase();
		Category cat = flowCategories.get(pref);
		if (cat == null) {
			cat = new Category(UUID.randomUUID().toString(), pref,
					Flow.class.getCanonicalName());
			BaseDao<Category> dao = database.createDao(Category.class);
			Category parent = dao.getForId(Flow.class.getCanonicalName());
			parent.add(cat);
			cat.setParentCategory(parent);
			dao.update(parent);
			flowCategories.put(pref, cat);
		}
		flow.setCategoryId(cat.getId());
	}

}
