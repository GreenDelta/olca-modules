package org.openlca.core.services;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.linking.LinkingConfig;
import org.openlca.core.matrix.linking.ProviderLinking;
import org.openlca.core.model.Currency;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.results.EnviFlowValue;
import org.openlca.core.results.ImpactValue;
import org.openlca.core.results.TechFlowValue;
import org.openlca.core.results.UpstreamNode;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.output.JsonRefs;
import org.openlca.util.Strings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

final class JsonUtil {

	private JsonUtil() {
	}

	static LinkingConfig linkingConfigOf(JsonObject json) {
		var conf = new LinkingConfig();
		if (json == null)
			return conf;
		var providerLinking = Json.getEnum(
				json, "providerLinking", ProviderLinking.class);
		if (providerLinking != null) {
			conf.providerLinking(providerLinking);
		}
		var preferUnitProcesses = Json.getBool(json, "preferUnitProcesses", false);
		conf.preferredType(preferUnitProcesses
				? ProcessType.UNIT_PROCESS
				: ProcessType.LCI_RESULT);
		Json.getDouble(json, "cutoff").ifPresent(conf::cutoff);
		return conf;
	}

	static JsonObject encodeTechFlow(TechFlow techFlow, JsonRefs refs) {
		if (techFlow == null)
			return null;
		var obj = new JsonObject();
		Json.put(obj, "provider", refs.asRef(techFlow.provider()));
		Json.put(obj, "flow", refs.asRef(techFlow.flow()));
		return obj;
	}

	static JsonObject encodeEnviFlow(EnviFlow enviFlow, JsonRefs refs) {
		if (enviFlow == null)
			return null;
		var obj = new JsonObject();
		Json.put(obj, "flow", refs.asRef(enviFlow.flow()));
		if (enviFlow.location() != null) {
			Json.put(obj, "location", refs.asRef(enviFlow.location()));
		}
		Json.put(obj, "isInput", enviFlow.isInput());
		if (enviFlow.isVirtual()) {
			Json.put(obj, "isVirtual", true);
			if (enviFlow.wrapped() instanceof RootDescriptor wrapped) {
				Json.put(obj, "wrapped", refs.asRef(wrapped));
			}
		}
		return obj;
	}

	static JsonObject encodeState(ResultState state) {
		var obj = new JsonObject();
		if (state == null || state.isEmpty()) {
			Json.put(obj, "error", "does not exist");
			return obj;
		}
		Json.put(obj, "@id", state.id());
		if (state.isError()) {
			Json.put(obj, "error", state.error());
			return obj;
		}
		Json.put(obj, "isReady", state.isReady());
		Json.put(obj, "isScheduled", state.isScheduled());
		Json.put(obj, "time", state.time());
		return obj;
	}

	static JsonObject encodeTechValue(TechFlowValue v, JsonRefs refs) {
		if (v == null)
			return null;
		var obj = new JsonObject();
		Json.put(obj, "techFlow", encodeTechFlow(v.techFlow(), refs));
		Json.put(obj, "amount", v.value());
		return obj;
	}

	static JsonArray encodeTechValues(Iterable<TechFlowValue> vs, JsonRefs refs) {
		return encodeArray(vs, v -> encodeTechValue(v, refs));
	}

	static JsonObject encodeEnviValue(EnviFlow flow, double value, JsonRefs refs) {
		return encodeEnviValue(new EnviFlowValue(flow, value), refs);
	}

	static JsonObject encodeEnviValue(EnviFlowValue v, JsonRefs refs) {
		if (v == null)
			return null;
		var obj = new JsonObject();
		Json.put(obj, "enviFlow", encodeEnviFlow(v.enviFlow(), refs));
		Json.put(obj, "amount", v.value());
		return obj;
	}

	static JsonArray encodeEnviValues(Iterable<EnviFlowValue> vs, JsonRefs refs) {
		return encodeArray(vs, v -> encodeEnviValue(v, refs));
	}

	static JsonObject encodeImpact(
			ImpactDescriptor impact, double value, JsonRefs refs) {
		return encodeImpact(ImpactValue.of(impact, value), refs);
	}

	static JsonObject encodeImpact(ImpactValue v, JsonRefs refs) {
		if (v == null)
			return null;
		var obj = new JsonObject();
		obj.add("impactCategory", refs.asRef(v.impact()));
		obj.addProperty("amount", v.value());
		return obj;
	}

	static JsonArray encodeImpactValues(Iterable<ImpactValue> vs, JsonRefs refs) {
		return encodeArray(vs, v -> encodeImpact(v, refs));
	}

	static JsonObject encodeCostValue(Currency c, double v) {
		var obj = new JsonObject();
		obj.add("currency", Json.asRef(c));
		obj.addProperty("amount", v);
		return obj;
	}

	static JsonObject encodeUpstreamNode(UpstreamNode node, JsonRefs refs) {
		if (node == null)
			return null;
		var obj = new JsonObject();
		Json.put(obj, "techFlow", encodeTechFlow(node.provider(), refs));
		Json.put(obj, "result", node.result());
		Json.put(obj, "directContribution", node.directContribution());
		Json.put(obj, "requiredAmount", node.requiredAmount());
		return obj;
	}

	static JsonArray encodeGroupValues(Map<String, Double> map) {
		if (map == null || map.isEmpty())
			return new JsonArray(0);
		record GroupVal(String group, double value) {
			JsonObject toJson() {
				var obj = new JsonObject();
				Json.put(obj, "group", group);
				Json.put(obj, "amount", value);
				return obj;
			}
		}
		var list = new ArrayList<GroupVal>(map.size());
		for (var e : map.entrySet()) {
			if (e.getKey() == null || e.getValue() == null)
				continue;
			list.add(new GroupVal(e.getKey(), e.getValue()));
		}
		list.sort((va, vb) -> Strings.compare(va.group, vb.group));
		return encodeArray(list, GroupVal::toJson);
	}

	static <T> JsonArray encodeArray(
			Iterable<T> items, Function<T, JsonObject> fn) {
		var array = new JsonArray();
		for (var next : items) {
			var json = fn.apply(next);
			if (json != null) {
				array.add(json);
			}
		}
		return array;
	}
}
