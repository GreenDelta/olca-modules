package org.openlca.jsonld.input;

import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ProjectImport extends BaseImport<Project> {

	private ProjectImport(String refId, ImportConfig conf) {
		super(ModelType.PROJECT, refId, conf);
	}

	static Project run(String refId, ImportConfig conf) {
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
		String authorRefId = In.getRefId(json, "author");
		p.setAuthor(ActorImport.run(authorRefId, conf));
		p.setCreationDate(In.getDate(json, "creationDate"));
		p.setFunctionalUnit(In.getString(json, "functionalUnit"));
		p.setGoal(In.getString(json, "goal"));
		p.setLastModificationDate(In.getDate(json, "lastModificationDate"));
		String methodRefId = In.getRefId(json, "impactMethod");
		ImpactMethod method = ImpactMethodImport.run(methodRefId, conf);
		if (method == null)
			return;
		p.setImpactMethodId(method.getId());
		String nwSetRefId = In.getRefId(json, "nwSet");
		for (NwSet set : method.nwSets)
			if (set.getRefId().equals(nwSetRefId)) {
				p.setNwSetId(set.getId());
				break;
			}
	}

	private void mapVariants(JsonObject json, Project p) {
		JsonArray array = In.getArray(json, "variants");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement element : array) {
			if (!element.isJsonObject())
				continue;
			JsonObject obj = element.getAsJsonObject();
			ProjectVariant v = new ProjectVariant();
			String systemRefId = In.getRefId(obj, "productSystem");
			ProductSystem system = ProductSystemImport.run(systemRefId, conf);
			if (system == null)
				continue;
			v.setProductSystem(system);
			String propRefId = In.getRefId(obj, "flowProperty");
			FlowPropertyFactor factor = findFlowPropertyFactor(propRefId,
					system);
			if (factor == null)
				continue;
			v.setFlowPropertyFactor(factor);
			String unitRefId = In.getRefId(obj, "unit");
			Unit unit = findUnit(unitRefId, factor);
			if (unit == null)
				continue;
			v.setUnit(unit);
			v.setName(In.getString(obj, "name"));
			v.setAmount(In.getDouble(obj, "amount", 0));
			v.setAllocationMethod(In.getEnum(obj, "allocationMethod",
					AllocationMethod.class));
			ParameterRedefs.addParameters(obj, v.getParameterRedefs(), conf);
			p.getVariants().add(v);
		}
	}

	private FlowPropertyFactor findFlowPropertyFactor(String propRefId,
			ProductSystem system) {
		if (system.getReferenceExchange() == null)
			return null;
		Flow product = system.getReferenceExchange().flow;
		for (FlowPropertyFactor factor : product.getFlowPropertyFactors())
			if (factor.getFlowProperty().getRefId().equals(propRefId))
				return factor;
		return null;
	}

	private Unit findUnit(String refId, FlowPropertyFactor factor) {
		UnitGroup ug = factor.getFlowProperty().getUnitGroup();
		if (ug == null)
			return null;
		for (Unit unit : ug.getUnits())
			if (unit.getRefId().equals(refId))
				return unit;
		return null;
	}

}
