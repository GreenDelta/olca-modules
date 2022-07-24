package org.openlca.ipc.handlers;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.gson.JsonElement;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.results.Contribution;
import org.openlca.core.results.FlowValue;
import org.openlca.core.results.ImpactValue;
import org.openlca.core.results.SimpleResult;

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

	static JsonObject encodeResult(SimpleResult r, String id, DbRefs refs) {
		var obj = new JsonObject();
		Json.put(obj, "@id", id);
		obj.addProperty("@id", id);
		if (r == null)
			return obj;
		Json.put(obj, "@type", r.getClass().getSimpleName());

		// tech. index, providers
		if (r.techIndex() != null) {
			var providers = r.techIndex()
					.stream()
					.map(techFlow -> encodeTechFlow(techFlow, refs))
					.collect(toArray());
			Json.put(obj, "providers", providers);
		}

		// flows & flow results
		if (r.hasEnviFlows()) {
			Json.put(obj, "flows", arrayOf(
					r.enviIndex(),
					enviFlow -> encodeEnviFlow(enviFlow, refs)));
			obj.add("flowResults",
					arrayOf(r.getTotalFlowResults(), v -> encodeFlowValue(v, refs)));
		}

		// impact categories and results
		if (r.hasImpacts()) {
			obj.add("impacts", arrayOf(r.impactIndex(), refs::asRef));
			obj.add("impactResults", arrayOf(
					r.getTotalImpactResults(), v -> encodeImpactValue(v, refs)));
		}

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

	static <T> JsonArray encodeContributions(
			Collection<Contribution<T>> contributions,
			Function<T, JsonElement> itemEncoder) {
		return contributions.stream()
				.map(c -> encodeContribution(c, itemEncoder))
				.collect(toArray());
	}

	static <T> JsonObject encodeContribution(Contribution<T> c,
			Function<T, JsonElement> itemEncoder) {
		if (c == null)
			return null;
		var obj = new JsonObject();
		Json.put(obj, "@type", "Contribution");
		if (c.item != null) {
			Json.put(obj, "item", itemEncoder.apply(c.item));
		}
		Json.put(obj, "amount", c.amount);
		Json.put(obj, "share", c.share);
		Json.put(obj, "unit", c.unit);
		Json.put(obj, "rest", c.isRest);
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

	public static JsonArrayCollector toArray() {
		return new JsonArrayCollector();
	}

	static class JsonArrayCollector
			implements Collector<JsonElement, JsonArray, JsonArray> {

		@Override
		public Supplier<JsonArray> supplier() {
			return JsonArray::new;
		}

		@Override
		public BiConsumer<JsonArray, JsonElement> accumulator() {
			return (array, element) -> {
				if (element != null) {
					array.add(element);
				}
			};
		}

		@Override
		public BinaryOperator<JsonArray> combiner() {
			return (array1, array2) -> {
				var combined = new JsonArray();
				combined.addAll(array1);
				combined.addAll(array2);
				return combined;
			};
		}

		@Override
		public Function<JsonArray, JsonArray> finisher() {
			return a -> a;
		}

		@Override
		public Set<Characteristics> characteristics() {
			return Set.of(Characteristics.IDENTITY_FINISH);
		}
	}

}
