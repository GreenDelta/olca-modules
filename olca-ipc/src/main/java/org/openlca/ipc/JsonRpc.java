package org.openlca.ipc;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.results.FlowResult;
import org.openlca.core.results.ImpactResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.core.results.SimpleResultProvider;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Some utility functions for en/decoding data in JSON-RPC.
 */
class JsonRpc {

	private JsonRpc() {
	}

	static JsonObject encode(SimpleResult r, String id, IDatabase db) {
		if (r == null)
			return null;
		JsonObject obj = new JsonObject();
		obj.addProperty("@type", "SimpleResult");
		obj.addProperty("@id", id);
		SimpleResultProvider<SimpleResult> provider = new SimpleResultProvider<>(
				r, EntityCache.create(db));
		JsonArray flowResults = new JsonArray();
		obj.add("flowResults", flowResults);
		for (FlowResult flowResult : provider.getTotalFlowResults()) {
			JsonObject item = JsonRpc.encode(flowResult, db);
			if (item != null) {
				flowResults.add(item);
			}
		}
		if (!r.hasImpactResults())
			return obj;
		JsonArray impactResults = new JsonArray();
		obj.add("impactResults", impactResults);
		for (ImpactResult impact : provider.getTotalImpactResults()) {
			JsonObject item = JsonRpc.encode(impact, db);
			if (item != null)
				impactResults.add(item);
		}
		return obj;
	}

	static JsonObject encode(FlowResult r, IDatabase db) {
		if (r == null)
			return null;
		JsonObject obj = new JsonObject();
		obj.addProperty("@type", "FlowResult");
		obj.add("flow", Json.asRef(r.flow, db));
		obj.addProperty("input", r.input);
		obj.addProperty("value", r.value);
		return obj;
	}

	static JsonObject encode(ImpactResult r, IDatabase db) {
		if (r == null)
			return null;
		JsonObject obj = new JsonObject();
		obj.addProperty("@type", "ImpactResult");
		obj.add("flow", Json.asRef(r.impactCategory, db));
		obj.addProperty("value", r.value);
		return obj;
	}

}
