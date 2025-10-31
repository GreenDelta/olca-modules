package org.openlca.core.services;

import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.CalculationTarget;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.input.ParameterReader;
import org.openlca.jsonld.input.Quantity;
import org.openlca.util.Strings;

public record JsonCalculationSetup(CalculationSetup setup, String error) {

	static JsonCalculationSetup error(String error) {
		return new JsonCalculationSetup(null, error);
	}

	static JsonCalculationSetup ok(CalculationSetup setup) {
		return new JsonCalculationSetup(setup, null);
	}

	public static JsonCalculationSetup readFrom(
			JsonObject obj, EntityResolver resolver) {
		if (obj == null)
			return error("setup object is empty");
		if (resolver == null)
			return error("no resolver provided");
		return new JsonCalculationSetup.Reader(obj, resolver).read();
	}

	public boolean hasError() {
		return error != null;
	}

	private record Reader(JsonObject json, EntityResolver resolver) {

		JsonCalculationSetup read() {
			var target = resolveTarget();
			if (target == null)
				return error("invalid calculation target");

			var setup = CalculationSetup.of(target);

			// LCIA method
			var methodId = Json.getRefId(json, "impactMethod");
			if (Strings.isNotBlank(methodId)) {
				var method = resolver.get(ImpactMethod.class, methodId);
				setup.withImpactMethod(method);
			}

			// nw-set
			var nwSetId = Json.getRefId(json, "nwSet");
			if (Strings.isNotBlank(nwSetId) && setup.impactMethod() != null) {
				var nwSet = setup.impactMethod().nwSets.stream()
						.filter(nws -> nwSetId.equals(nws.refId))
						.findAny()
						.orElse(null);
				setup.withNwSet(nwSet);
			}

			// amount & quantity
			Json.getDouble(json, "amount").ifPresent(setup::withAmount);
			var flow = setup.flow();
			if (flow == null)
				return error("no reference flow defined");
			var q = Quantity.of(flow, json);
			setup.withFlowPropertyFactor(q.factor());
			setup.withUnit(q.unit());

			// other attributes
			var alloc = Json.getEnum(
					json, "allocation", AllocationMethod.class);
			setup.withAllocation(alloc);
			setup.withCosts(Json.getBool(json, "withCosts", false));
			setup.withRegionalization(
					Json.getBool(json, "withRegionalization", false));

			// parameters
			addParameters(setup);

			return ok(setup);
		}

		private CalculationTarget resolveTarget() {
			var ref = Json.getObject(json, "target");
			if (ref != null) {
				var refId = Json.getString(ref, "@id");
				if (Strings.isBlank(refId)) {
					return null;
				}
				var type = Json.getString(ref, "@type");
				if (Objects.equals(type, "Process")) {
					var process = resolver.get(Process.class, refId);
					if (process != null)
						return process;
				}
				return resolver.get(ProductSystem.class, refId);
			}
			var systemId = Json.getRefId(json, "productSystem");
			if (Strings.isNotBlank(systemId))
				return resolver.get(ProductSystem.class, systemId);
			var processId = Json.getRefId(json, "process");
			return Strings.isNotBlank(processId)
					? resolver.get(Process.class, processId)
					: null;
		}

		private void addParameters(CalculationSetup setup) {
			var array = Json.getArray(json, "parameters");
			if (array != null) {
				var redefs = ParameterReader.readRedefs(array, resolver);
				setup.withParameters(redefs);
			} else {
				// if no parameter redefinitions are defined, we take the redefinitions
				// of the baseline set by default if this is defined
				var target = setup.target();
				if (target.isProductSystem()) {
					target.asProductSystem().parameterSets
							.stream()
							.filter(s -> s.isBaseline)
							.findAny()
							.ifPresent(redefs -> setup.withParameters(redefs.parameters));
				}
			}
		}
	}
}
