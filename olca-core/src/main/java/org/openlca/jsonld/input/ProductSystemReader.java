package org.openlca.jsonld.input;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Result;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.jsonld.Json;

public class ProductSystemReader implements EntityReader<ProductSystem> {

	private final EntityResolver resolver;
	private final Map<String, Descriptor> processes = new HashMap<>();
	private final Map<String, Descriptor> flows = new HashMap<>();

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

		// clear resources in case the reader is re-used
		processes.clear();
		flows.clear();

		Util.mapBase(system, json, resolver);
		mapQRef(json, system);
		addParameterSets(json, system);

		// add processes and cache them
		system.processes.clear();
		Json.forEachObject(
			json, "processes", obj -> addProcess(system, obj));

		// add link references and cache them
		system.processLinks.clear();
		Json.forEachObject(json, "processLinks", obj -> {
			addProcess(system, Json.getObject(obj, "provider"));
			addProcess(system, Json.getObject(obj, "process"));
			String flowRefId = Json.getRefId(obj, "flow");
			var flow = resolver.getDescriptor(Flow.class, flowRefId);
			if (flow != null) {
				flows.put(flowRefId, flow);
			}
		});

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

	private void addParameterSets(JsonObject json, ProductSystem sys) {
		sys.parameterSets.clear();
		Json.forEachObject(json, "parameterSets", obj -> {
			var set = new ParameterRedefSet();
			sys.parameterSets.add(set);
			set.name = Json.getString(obj, "name");
			set.description = Json.getString(obj, "description");
			set.isBaseline = Json.getBool(obj, "isBaseline", false);
			var redefs = Json.getArray(obj, "parameters");
			if (redefs != null && redefs.size() > 0) {
				set.parameters.addAll(ParameterRedefs.read(redefs, resolver));
			}
		});
	}

	private void addProcess(ProductSystem s, JsonObject ref) {
		if (ref == null)
			return;
		String refId = Json.getString(ref, "@id");
		if (refId == null || processes.containsKey(refId))
			return;

		String type = Json.getString(ref, "@type");
		var clazz = type == null
			? Process.class
			: switch (type) {
			case "ProductSystem" -> ProductSystem.class;
			case "Result" -> Result.class;
			default -> Process.class;
		};

		var p = resolver.getDescriptor(clazz, refId);
		if (p != null) {
			processes.put(refId, p);
			s.processes.add(p.id);
		}
	}
}
