package org.openlca.ipc.handlers;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gson.JsonElement;
import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.results.Contribution;
import org.openlca.core.results.FlowValue;
import org.openlca.core.results.ImpactValue;
import org.openlca.core.results.SimpleResult;
import org.openlca.core.results.UpstreamNode;
import org.openlca.core.results.UpstreamTree;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.output.DbRefs;

/**
 * Some utility functions for en/decoding data in JSON-RPC.
 */
class JsonRpc {

	private JsonRpc() {
	}

	static JsonObject encode(SimpleResult r, String id, DbRefs refs) {
		var obj = new JsonObject();
		Json.put(obj, "@id", id);
		obj.addProperty("@id", id);
		if (r == null)
			return obj;
		Json.put(obj, "@type", r.getClass().getSimpleName());

		Json.put(obj, "providers",
			arrayOf(r.techIndex(), techFlow -> encodeTechFlow(techFlow, refs)));

		// flows & flow results
		if (!r.hasEnviFlows())
			return obj;
		Json.put(obj, "flows",
			arrayOf(r.enviIndex(), enviFlow -> encodeEnviFlow(enviFlow, cache)));
		obj.add("flowResults",
			arrayOf(r.getTotalFlowResults(), v -> encodeFlowValue(v, cache)));

		// impact categories and results
		if (!r.hasImpacts())
			return obj;
		obj.add("impacts",
			arrayOf(r.impactIndex(), e -> JsonRef.of(e, cache)));
		obj.add("impactResults",
			arrayOf(r.getTotalImpactResults(), v -> encodeImpactValue(v, cache)));

		return obj;
	}

	static JsonObject encodeEnviFlow(EnviFlow ef, DbRefs refs) {
		var obj = new JsonObject();
		Json.put(obj, "flow", refs.asRef(ef.flow()));
		Json.put(obj, "location", refs.asRef(ef.location()));
		Json.put(obj, "isInput", ef.isInput());
		return obj;
	}

	static JsonObject encodeTechFlow(TechFlow techFlow, DbRefs refs) {
		var obj = new JsonObject();
		Json.put(obj, "provider", refs.asRef(techFlow.provider()));
		Json.put(obj, "flow", refs.asRef(techFlow.flow()));
		return obj;
	}

	static JsonObject encodeFlowValue(FlowValue r, DbRefs refs) {
		if (r == null)
			return null;
		JsonObject obj = new JsonObject();
		obj.addProperty("@type", "FlowResult");
		obj.add("flow", encodeEnviFlow(r.indexFlow(), refs));
		obj.addProperty("value", r.value());
		return obj;
	}

	static JsonObject encodeImpactValue(ImpactValue r, DbRefs refs) {
		if (r == null)
			return null;
		JsonObject obj = new JsonObject();
		obj.addProperty("@type", "ImpactResult");
		obj.add("impactCategory", refs.asRef(r.impact()));
		obj.addProperty("value", r.value());
		return obj;
	}

	static <T extends Descriptor> JsonArray encode(
		Collection<Contribution<T>> l, EntityCache cache, Consumer<JsonObject> modifier) {
		if (l == null)
			return null;
		return encode(l, contribution -> encode(contribution, cache, modifier));
	}

	static <T> JsonObject encode(Contribution<T> i,
			Consumer<JsonObject> modifier) {
		if (i == null)
			return null;
		JsonObject obj = new JsonObject();
		obj.addProperty("@type", "ContributionItem");
		obj.add("item", JsonRef.of(i.item, cache));
		obj.addProperty("amount", i.amount);
		obj.addProperty("share", i.share);
		obj.addProperty("rest", i.isRest);
		modifier.accept(obj);
		return obj;
	}

	static <T> JsonArray arrayOf(
		Iterable<T> elements, Function<T, ? extends JsonElement> fn) {
		var array = new JsonArray();
		if (elements == null || fn == null)
			return array;
		for (var elem : elements) {
			var jsonElem = fn.apply(elem);
			if (jsonElem != null) {
				array.add(jsonElem);
			}
		}
		return array;
	}

	static <T extends Descriptor> JsonArray encode(Collection<UpstreamNode> l, UpstreamTree tree, EntityCache cache, Consumer<JsonObject> modifier) {
		if (l == null)
			return null;
		return encode(l, node -> encode(node, tree.root.result(), cache, json -> {
			json.addProperty("hasChildren", !tree.childs(node).isEmpty());
			modifier.accept(json);
		}));
	}

	static <T> JsonObject encode(UpstreamNode n, double total, EntityCache cache, Consumer<JsonObject> modifier) {
		if (n == null)
			return null;
		JsonObject obj = new JsonObject();
		obj.addProperty("@type", "ContributionItem");
		obj.add("item", JsonRef.of(n.provider().flow(), cache));
		obj.add("owner", JsonRef.of(n.provider().provider(), cache));
		obj.addProperty("amount", n.result());
		obj.addProperty("share", total != 0 ? n.result() / total : 0);
		modifier.accept(obj);
		return obj;
	}

	static JsonArray encode(double[] totalRequirements, double[] costs, TechIndex index, EntityCache cache) {
		JsonArray items = new JsonArray();
		for (int i = 0; i < totalRequirements.length; i++) {
			if (totalRequirements[i] == 0)
				continue;
			var product = index.at(i);
			JsonObject obj = new JsonObject();
			obj.add("process", JsonRef.of(product.provider(), cache));
			obj.add("product", JsonRef.of(product.flow(), cache));
			obj.addProperty("amount", totalRequirements[i]);
			if (costs != null) {
				obj.addProperty("costs", costs[i]);
			}
			items.add(obj);
		}
		return items;
	}

}
