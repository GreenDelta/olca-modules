package org.openlca.ipc.handlers;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.index.EnviFlow;
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

/**
 * Some utility functions for en/decoding data in JSON-RPC.
 */
class JsonRpc {

	private JsonRpc() {
	}

	static JsonObject encode(SimpleResult r, String id, EntityCache cache) {
		JsonObject obj = new JsonObject();
		obj.addProperty("@id", id);
		if (r == null)
			return obj;
		obj.addProperty("@type", r.getClass().getSimpleName());
		obj.add("flows", encode(
				r.getFlows().stream()
						.map(EnviFlow::flow)
						.collect(Collectors.toSet()),
				cache));
		obj.add("processes", encode(r.getProcesses(), cache));
		obj.add("flowResults", encode(r.getTotalFlowResults(), result -> encode(result, cache)));
		if (!r.hasImpacts())
			return obj;
		obj.add("impacts", encode(r.getImpacts(), cache));
		obj.add("impactResults", encode(r.getTotalImpactResults(), result -> encode(result, cache)));
		return obj;
	}

	static JsonObject encode(FlowValue r, EntityCache cache) {
		if (r == null)
			return null;
		JsonObject obj = new JsonObject();
		obj.addProperty("@type", "FlowResult");
		obj.add("flow", JsonRef.of(r.flow(), cache));
		obj.addProperty("input", r.isInput());
		obj.addProperty("value", r.value());
		return obj;
	}

	static JsonObject encode(ImpactValue r, EntityCache cache) {
		if (r == null)
			return null;
		JsonObject obj = new JsonObject();
		obj.addProperty("@type", "ImpactResult");
		obj.add("impactCategory", JsonRef.of(r.impact(), cache));
		obj.addProperty("value", r.value());
		return obj;
	}

	static <T extends Descriptor> JsonArray encode(Collection<Contribution<T>> l, EntityCache cache, Consumer<JsonObject> modifier) {
		if (l == null)
			return null;
		return encode(l, contribution -> encode(contribution, cache, modifier));
	}

	static <T extends Descriptor> JsonObject encode(Contribution<T> i, EntityCache cache,
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

	static JsonArray encode(Collection<? extends Descriptor> descriptors, EntityCache cache) {
		return encode(descriptors, d -> JsonRef.of(d, cache));
	}

	static <T> JsonArray encode(Collection<T> l, Function<T, JsonObject> encoder) {
		JsonArray array = new JsonArray();
		for (T t : l) {
			JsonObject item = encoder.apply(t);
			if (item != null) {
				array.add(item);
			}
		}
		return array;
	}

}
