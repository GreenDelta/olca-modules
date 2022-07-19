package org.openlca.jsonld.input;

import java.util.Objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Result;
import org.openlca.jsonld.Json;

public record ProductSystemReader(EntityResolver resolver)
	implements EntityReader<ProductSystem> {

	public ProductSystemReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public ProductSystem read(JsonObject json) {
		var system = new ProductSystem();
		update(system, json);
		return system;
	}

	@Override
	public void update(ProductSystem system, JsonObject json) {
		Util.mapBase(system, json, resolver);
		mapQRef(json, system);

		addProcesses(json, system);
		addParameterSets(json, system);
		importLinkRefs(json, system);
		ProductSystemLinks.map(json, resolver, system);
	}

	private void mapQRef(JsonObject json, ProductSystem system) {
		var refProcessId = Json.getRefId(json, "refProcess");
		system.targetAmount = Json.getDouble(json, "targetAmount", 1d);
		system.referenceProcess = resolver.get(Process.class, refProcessId);

		Runnable clearQRef = () -> {
			system.referenceExchange = null;
			system.targetFlowPropertyFactor = null;
			system.targetUnit = null;
		};

		if (system.referenceProcess == null) {
			clearQRef.run();
			return;
		}

		Exchange qRef = null;
		var exchangeRef = Json.getObject(json, "refExchange");
		if (exchangeRef != null) {
			var exchangeId = Json.getInt(exchangeRef, "internalId");
			if (exchangeId.isEmpty()) {
				clearQRef.run();
				return;
			}
			var eid = exchangeId.getAsInt();
			qRef = system.referenceProcess.exchanges.stream()
				.filter(e -> e.internalId == eid)
				.findAny()
				.orElse(null);
			if (qRef == null) {
				clearQRef.run();
				return;
			}
		}

		system.referenceExchange = qRef == null
			? system.referenceProcess.quantitativeReference
			: null;
		if (qRef == null || qRef.flow == null) {
			clearQRef.run();
			return;
		}

		var quantity = Quantity.of(
			qRef.flow, json, "targetFlowProperty", "targetUnit");
		system.targetFlowPropertyFactor = quantity.factor();
		system.targetUnit = quantity.unit();
	}

	private void addProcesses(JsonObject json, ProductSystem s) {
		s.processes.clear();
		var array = Json.getArray(json, "processes");
		if (array == null || array.size() == 0)
			return;
		for (var e : array) {
			if (e.isJsonObject()) {
				addProcess(s, e.getAsJsonObject());
			}
		}
	}

	private void addProcess(ProductSystem s, JsonObject ref) {
		if (ref == null)
			return;
		String refId = Json.getString(ref, "@id");
		String type = Json.getString(ref, "@type");
		if (refId == null || type == null)
			return;
		var clazz = switch (type) {
			case "ProductSystem" -> ProductSystem.class;
			case "Result" -> Result.class;
			default -> Process.class;
		};
		var p = resolver.get(clazz, refId);
		if (p != null) {
			s.processes.add(p.id);
		}
	}

	private void importLinkRefs(JsonObject json, ProductSystem s) {
		s.processLinks.clear();
		var array = Json.getArray(json, "processLinks");
		if (array == null || array.size() == 0)
			return;
		for (var element : array) {
			if (!element.isJsonObject())
				continue;
			var obj = element.getAsJsonObject();
			String flowRefId = Json.getRefId(obj, "flow");
			resolver.get(Flow.class, flowRefId);
			addProcess(s, Json.getObject(obj, "provider"));
			addProcess(s, Json.getObject(obj, "process"));
		}
	}

	private void addParameterSets(JsonObject json, ProductSystem sys) {
		sys.parameterSets.clear();
		var array = Json.getArray(json, "parameterSets");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement elem : array) {
			if (!elem.isJsonObject())
				continue;
			var obj = elem.getAsJsonObject();
			var set = new ParameterRedefSet();
			sys.parameterSets.add(set);
			set.name = Json.getString(obj, "name");
			set.description = Json.getString(obj, "description");
			set.isBaseline = Json.getBool(obj, "isBaseline", false);
			JsonArray redefs = Json.getArray(obj, "parameters");
			if (redefs != null && redefs.size() > 0) {
				set.parameters.addAll(ParameterRedefs.read(redefs, resolver));
			}
		}
	}
}
