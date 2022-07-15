package org.openlca.jsonld.input;

import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.jsonld.Json;

public record ProjectReader(EntityResolver resolver)
	implements EntityReader<Project> {

	public ProjectReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public Project read(JsonObject json) {
		var project = new Project();
		update(project, json);
		return project;
	}

	@Override
	public void update(Project project, JsonObject json) {
		Util.mapBase(project, json, resolver);
		mapAttrs(json, project);
		mapVariants(json, project);
	}

	private void mapAttrs(JsonObject json, Project p) {
		p.isWithCosts = Json.getBool(json, "isWithCosts", false);
		p.isWithRegionalization = Json.getBool(json, "isWithRegionalization", false);
		var methodId = Json.getRefId(json, "impactMethod");
		p.impactMethod = resolver.get(ImpactMethod.class, methodId);
		var nwSetId = Json.getRefId(json, "nwSet");
		if (p.impactMethod == null || nwSetId == null) {
			p.nwSet = null;
			return;
		}
		p.nwSet = p.impactMethod.nwSets.stream()
			.filter(nwSet -> nwSetId.equals(nwSet.refId))
			.findAny()
			.orElse(null);
	}

	private void mapVariants(JsonObject json, Project p) {
		p.variants.clear();
		var array = Json.getArray(json, "variants");
		if (array == null || array.size() == 0)
			return;
		for (var e : array) {
			if (!e.isJsonObject())
				continue;
			var obj = e.getAsJsonObject();
			var v = new ProjectVariant();
			var systemRefId = Json.getRefId(obj, "productSystem");
			var system = resolver.get(ProductSystem.class, systemRefId);
			if (system == null)
				continue;
			v.productSystem = system;

			// flow property and unit
			var flow = v.productSystem.referenceExchange != null
				? v.productSystem.referenceExchange.flow
				: null;
			if (flow == null)
				continue;
			var quantity = Quantity.of(flow, obj);
			v.flowPropertyFactor = quantity.factor();
			v.unit = quantity.unit();

			v.name = Json.getString(obj, "name");
			v.amount = Json.getDouble(obj, "amount", 0);
			v.allocationMethod = Json.getEnum(
				obj, "allocationMethod", AllocationMethod.class);
			v.description = Json.getString(obj, "description");
			v.isDisabled = Json.getBool(obj, "isDisabled", false);

			// parameter redefinitions
			var redefs = Json.getArray(obj, "parameterRedefs");
			if (redefs != null && redefs.size() > 0) {
				v.parameterRedefs.addAll(ParameterRedefs.read(redefs, resolver));
			}

			p.variants.add(v);
		}
	}
}
