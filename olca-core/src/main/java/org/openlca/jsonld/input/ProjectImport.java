package org.openlca.jsonld.input;

import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
		String methodID = Json.getRefId(json, "impactMethod");
		if (methodID == null)
			return;
		p.impactMethod = ImpactMethodImport.run(methodID, conf);
		if (p.impactMethod == null)
			return;
		String nwSetID = Json.getRefId(json, "nwSet");
		if (nwSetID == null)
			return;
		p.nwSet = p.impactMethod.nwSets.stream()
				.filter(nwSet -> nwSetID.equals(nwSet.refId))
				.findAny()
				.orElse(null);
	}

	private void mapVariants(JsonObject json, Project p) {
		JsonArray array = Json.getArray(json, "variants");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement element : array) {
			if (!element.isJsonObject())
				continue;
			JsonObject obj = element.getAsJsonObject();
			ProjectVariant v = new ProjectVariant();
			String systemRefId = Json.getRefId(obj, "productSystem");
			ProductSystem system = ProductSystemImport.run(systemRefId, conf);
			if (system == null)
				continue;
			v.productSystem = system;
			String propRefId = Json.getRefId(obj, "flowProperty");
			FlowPropertyFactor factor = findFlowPropertyFactor(propRefId,
					system);
			if (factor == null)
				continue;
			v.flowPropertyFactor = factor;
			String unitRefId = Json.getRefId(obj, "unit");
			Unit unit = findUnit(unitRefId, factor);
			if (unit == null)
				continue;
			v.unit = unit;
			v.name = Json.getString(obj, "name");
			v.amount = Json.getDouble(obj, "amount", 0);
			v.allocationMethod = Json.getEnum(obj, "allocationMethod",
					AllocationMethod.class);

			// parameter redefinitions
			JsonArray redefs = Json.getArray(obj, "parameterRedefs");
			if (redefs != null && redefs.size() > 0) {
				v.parameterRedefs.addAll(
						ParameterRedefs.read(redefs, conf));
			}

			p.variants.add(v);
		}
	}

	private FlowPropertyFactor findFlowPropertyFactor(String propRefId,
													  ProductSystem system) {
		if (system.referenceExchange == null)
			return null;
		Flow product = system.referenceExchange.flow;
		for (FlowPropertyFactor factor : product.flowPropertyFactors)
			if (factor.flowProperty.refId.equals(propRefId))
				return factor;
		return null;
	}

	private Unit findUnit(String refId, FlowPropertyFactor factor) {
		UnitGroup ug = factor.flowProperty.unitGroup;
		if (ug == null)
			return null;
		for (Unit unit : ug.units)
			if (unit.refId.equals(refId))
				return unit;
		return null;
	}

}
