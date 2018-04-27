package org.openlca.jsonld.input;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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

/**
 * Maps the reference exchange and exchange IDs of the process links of a
 * product system and generates the process links. This function should be
 * called at the end of a product system import when all the referenced data are
 * already imported.
 */
class ProductSystemExchanges {

	private final Logger log = LoggerFactory.getLogger(ProductSystemExchanges.class);
	private final IDatabase db;
	private final RefIdMap<String, Long> refIds;
	private final Map<Long, Map<Integer, Long>> exchangeIds;

	private ProductSystemExchanges(ImportConfig conf) {
		db = conf.db.getDatabase();
		refIds = RefIdMap.refToInternal(db, Process.class, Flow.class, Unit.class);
		exchangeIds = new HashMap<>();
		try {
			NativeSql.on(db).query("SELECT f_owner, id, internal_id FROM tbl_exchanges", this::putExchangeId);
		} catch (SQLException e) {
			log.error("Error loading exchange ids", e);
		}
	}

	private boolean putExchangeId(ResultSet rs) throws SQLException {
		long processId = rs.getLong("f_owner");
		long exchangeId = rs.getLong("id");
		int internalId = rs.getInt("internal_id");
		Map<Integer, Long> ofProcess = exchangeIds.get(processId);
		if (ofProcess == null) {
			exchangeIds.put(processId, ofProcess = new HashMap<>());
		}
		ofProcess.put(internalId, exchangeId);
		return true;
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
			JsonObject exchange = In.getObject(obj, "exchange");
			int internalId = In.getInt(exchange, "internalId", 0);
			link.exchangeId = findExchangeId(link.processId, internalId);
			if (valid(link)) {
				system.processLinks.add(link);
			}
		}
	}

	private boolean valid(ProcessLink link) {
		return link.providerId != 0 && link.processId != 0
				&& link.flowId != 0 && link.exchangeId != 0;
	}

	private void setReferenceExchange(JsonObject json, ProductSystem system) {
		Process refProcess = system.referenceProcess;
		if (refProcess == null)
			return;
		JsonObject refJson = In.getObject(json, "referenceExchange");
		int internalId = In.getInt(refJson, "internalId", 0);
		if (internalId <= 0)
			return;
		Exchange refExchange = refProcess.getExchange(internalId);
		system.referenceExchange = refExchange;
		system.targetFlowPropertyFactor = findFactor(json, system);
		system.targetUnit = findUnit(json, system);
	}

	private FlowPropertyFactor findFactor(JsonObject json, ProductSystem s) {
		Exchange e = s.referenceExchange;
		if (e == null)
			return null;
		String propertyRefId = In.getRefId(json, "targetFlowProperty");
		for (FlowPropertyFactor f : e.flow.getFlowPropertyFactors())
			if (f.getFlowProperty().getRefId().equals(propertyRefId))
				return f;
		return null;
	}

	private Unit findUnit(JsonObject json, ProductSystem s) {
		FlowPropertyFactor f = s.targetFlowPropertyFactor;
		if (f == null)
			return null;
		String unitRefId = In.getRefId(json, "targetUnit");
		UnitGroup ug = f.getFlowProperty().getUnitGroup();
		for (Unit u : ug.getUnits())
			if (u.getRefId().equals(unitRefId))
				return u;
		return null;
	}

	private long getId(JsonObject json, String key, Class<?> type) {
		JsonObject refObj = In.getObject(json, key);
		if (refObj == null)
			return 0;
		String refId = In.getString(refObj, "@id");
		Long id = refIds.get(type, refId);
		return id == null ? 0 : id;
	}

	private long findExchangeId(long processId, int internalId) {
		Map<Integer, Long> ofProcess = exchangeIds.get(processId);
		if (ofProcess == null)
			return 0;
		if (ofProcess.get(internalId) == null)
			return 0;
		return ofProcess.get(internalId);
	}

}
