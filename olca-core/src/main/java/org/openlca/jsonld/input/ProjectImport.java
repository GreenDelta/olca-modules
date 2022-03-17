package org.openlca.jsonld.input;

import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;
import org.openlca.util.Strings;

class ProjectImport extends BaseImport<Project> {

	private ProjectImport(String refId, JsonImport conf) {
		super(ModelType.PROJECT, refId, conf);
	}

	static Project run(String refId, JsonImport conf) {
		return new ProjectImport(refId, conf).run();
	}

	@Override
	Project map(JsonObject json, long id) {
		if (json == null)
			return null;
		Project p = new Project();
		In.mapAtts(json, p, id, conf);
		mapAtts(json, p);
		mapVariants(json, p);
		return conf.db.put(p);
	}

	private void mapAtts(JsonObject json, Project p) {
		p.isWithCosts = Json.getBool(json, "isWithCosts", false);
		p.isWithRegionalization = Json.getBool(json, "isWithRegionalization", false);

		// LCIA method and NW set
		var methodId = Json.getRefId(json, "impactMethod");
		if (methodId == null)
			return;
		p.impactMethod = ImpactMethodImport.run(methodId, conf);
		if (p.impactMethod == null)
			return;
		var nwSetId = Json.getRefId(json, "nwSet");
		if (nwSetId == null)
			return;
		p.nwSet = p.impactMethod.nwSets.stream()
			.filter(nwSet -> nwSetId.equals(nwSet.refId))
			.findAny()
			.orElse(null);
	}

	private void mapVariants(JsonObject json, Project p) {
		var array = Json.getArray(json, "variants");
		if (array == null || array.size() == 0)
			return;
		for (var e : array) {
			if (!e.isJsonObject())
				continue;
			var obj = e.getAsJsonObject();
			var v = new ProjectVariant();
			var systemRefId = Json.getRefId(obj, "productSystem");
			var system = ProductSystemImport.run(systemRefId, conf);
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
				v.parameterRedefs.addAll(ParameterRedefs.read(redefs, conf));
			}

			p.variants.add(v);
		}
	}
}
