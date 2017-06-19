package org.openlca.jsonld.output;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ProjectWriter extends Writer<Project> {

	ProjectWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	JsonObject write(Project p) {
		JsonObject obj = super.write(p);
		if (obj == null)
			return null;
		Out.put(obj, "creationDate", p.getCreationDate());
		Out.put(obj, "functionalUnit", p.getFunctionalUnit());
		Out.put(obj, "goal", p.getGoal());
		Out.put(obj, "lastModificationDate", p.getLastModificationDate());
		Out.put(obj, "author", p.getAuthor(), conf);
		Out.put(obj, "impactMethod", createRef(ModelType.IMPACT_METHOD, p.getImpactMethodId()));
		Out.put(obj, "nwSet", createRef(ModelType.NW_SET, p.getNwSetId()));
		mapVariants(obj, p);
		ParameterReferences.writeReferencedParameters(p, conf);
		return obj;
	}

	private void mapVariants(JsonObject json, Project p) {
		JsonArray array = new JsonArray();
		for (ProjectVariant v : p.getVariants()) {
			JsonObject obj = new JsonObject();
			Out.put(obj, "@type", ProjectVariant.class.getSimpleName());
			Out.put(obj, "name", v.getName());
			Out.put(obj, "productSystem", v.getProductSystem(), conf, Out.REQUIRED_FIELD);
			Out.put(obj, "amount", v.getAmount());
			Out.put(obj, "unit", v.getUnit(), conf, Out.REQUIRED_FIELD);
			Out.put(obj, "allocationMethod", v.getAllocationMethod());
			FlowProperty prop = null;
			if (v.getFlowPropertyFactor() != null)
				prop = v.getFlowPropertyFactor().getFlowProperty();
			Out.put(obj, "flowProperty", prop, conf, Out.REQUIRED_FIELD);
			ParameterRedefs.map(obj, v.getParameterRedefs(), conf.db, conf, this::createRef);
			array.add(obj);
		}
		Out.put(json, "variants", array);
	}

	private JsonObject createRef(ModelType type, Long id) {
		if (id == null)
			return null;
		if (type == null)
			return null;
		return References.create(type, id, conf, true);
	}

}
