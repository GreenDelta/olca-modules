package org.openlca.jsonld.input;

import java.sql.SQLException;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.util.RefIdMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gnu.trove.map.hash.TLongDoubleHashMap;

/**
 * Maps the reference exchange and exchange IDs of the process links of a
 * product system and generates the process links. This function should be
 * called at the end of a product system import when all the referenced data are
 * already imported.
 */
class ProductSystemExchanges {

	private final IDatabase db;
	private final RefIdMap<String, Long> refIds;

	private ProductSystemExchanges(ImportConfig conf) {
		db = conf.db.getDatabase();
		refIds = RefIdMap.refToInternal(db, Process.class, Flow.class, Unit.class);
	}

	static void map(JsonObject json, ImportConfig conf, ProductSystem system) {
		new ProductSystemExchanges(conf).map(json, system);
	}

	private void map(JsonObject json, ProductSystem system) {
		if (json == null || system == null)
			return;
		setReferenceExchange(json, system);
		JsonArray array = In.getArray(json, "processLinks");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement element : array) {
			JsonObject obj = element.getAsJsonObject();
			ProcessLink link = new ProcessLink();
			link.providerId = getId(obj, "provider", Process.class);
			link.processId = getId(obj, "process", Process.class);
			link.flowId = getId(obj, "flow", Flow.class);
			link.exchangeId = findExchangeId(link.processId,
					In.getObject(obj, "exchange"));
			if (valid(link)) {
				system.getProcessLinks().add(link);
			}
		}
	}

	private boolean valid(ProcessLink link) {
		return link.providerId != 0 && link.processId != 0
				&& link.flowId != 0 && link.exchangeId != 0;
	}

	private void setReferenceExchange(JsonObject json, ProductSystem system) {
		Process refProcess = system.getReferenceProcess();
		if (refProcess == null)
			return;
		JsonObject refJson = In.getObject(json, "referenceExchange");
		long id = findExchangeId(refProcess.getId(), refJson);
		if (id <= 0)
			return;
		Exchange refExchange = db.createDao(Exchange.class).getForId(id);
		system.setReferenceExchange(refExchange);
		system.setTargetFlowPropertyFactor(findFactor(json, system));
		system.setTargetUnit(findUnit(json, system));
	}

	private FlowPropertyFactor findFactor(JsonObject json, ProductSystem s) {
		Exchange e = s.getReferenceExchange();
		if (e == null)
			return null;
		String propertyRefId = In.getRefId(json, "targetFlowProperty");
		for (FlowPropertyFactor f : e.flow.getFlowPropertyFactors())
			if (f.getFlowProperty().getRefId().equals(propertyRefId))
				return f;
		return null;
	}

	private Unit findUnit(JsonObject json, ProductSystem s) {
		FlowPropertyFactor f = s.getTargetFlowPropertyFactor();
		if (f == null)
			return null;
		String unitRefId = In.getRefId(json, "targetUnit");
		UnitGroup ug = f.getFlowProperty().getUnitGroup();
		for (Unit u : ug.getUnits())
			if (u.getRefId().equals(unitRefId))
				return u;
		return null;
	}

	/**
	 * Try to find an exchange of the given process with the matching attributes
	 * from the database. Returns -1 if nothing were found
	 */
	private long findExchangeId(long processId, JsonObject json) {
		if (processId <= 0 || json == null)
			return -1;
		long flowId = getId(json, "flow", Flow.class);
		long unitId = getId(json, "unit", Unit.class);
		long providerId = getId(json, "defaultProvider", Process.class);
		int input = In.getBool(json, "input", true) ? 1 : 0;
		double amount = In.getDouble(json, "amount", 0);
		String sql = "select id, resulting_amount_value from tbl_exchanges "
				+ "where f_owner=" + processId + " and f_flow=" + flowId
				+ " and f_unit=" + unitId + " and f_default_provider="
				+ providerId + " and is_input=" + input;
		try {
			return queryId(amount, sql);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to search exchange: " + sql, e);
			return -1;
		}
	}

	private long queryId(double amount, String sql) throws SQLException {
		TLongDoubleHashMap vals = new TLongDoubleHashMap();
		NativeSql.on(db).query(sql, r -> {
			vals.put(r.getLong(1), r.getDouble(2));
			return true;
		});
		long exchangeId = -1;
		double delta = 0;
		for (long id : vals.keys()) {
			double dist = Math.abs(amount - vals.get(id));
			if (exchangeId == -1 || dist < delta) {
				exchangeId = id;
				delta = dist;
			}
		}
		return exchangeId;
	}

	private long getId(JsonObject json, String key, Class<?> type) {
		JsonObject refObj = In.getObject(json, key);
		if (refObj == null)
			return 0;
		String refId = In.getString(refObj, "@id");
		Long id = refIds.get(type, refId);
		return id == null ? 0 : id;
	}
}
