package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.output.ExportConfig.ProjectOption;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ProjectWriter extends Writer<Project> {

	private final ExportConfig conf;
	private final boolean exportReferences;
	private Consumer<RootEntity> refFn;

	ProjectWriter(ExportConfig conf) {
		this.conf = conf;
		exportReferences = conf.projectOption == ProjectOption.INCLUDE_REFERENCES;
	}

	@Override
	JsonObject write(Project p, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(p, refFn);
		if (obj == null)
			return null;
		this.refFn = refFn;
		Out.put(obj, "creationDate", p.getCreationDate());
		Out.put(obj, "functionalUnit", p.getFunctionalUnit());
		Out.put(obj, "goal", p.getGoal());
		Out.put(obj, "lastModificationDate", p.getLastModificationDate());
		Out.put(obj, "author", p.getAuthor(), refFn);
		Out.put(obj, "impactMethod",
				createRef(ModelType.IMPACT_METHOD, p.getImpactMethodId()));
		Out.put(obj, "nwSet", createRef(ModelType.NW_SET, p.getNwSetId()));
		mapVariants(obj, p, refFn);
		return obj;
	}

	private void mapVariants(JsonObject json, Project p,
			Consumer<RootEntity> refFn) {
		JsonArray array = new JsonArray();
		for (ProjectVariant v : p.getVariants()) {
			JsonObject obj = new JsonObject();
			Out.put(obj, "name", v.getName());
			if (exportReferences)
				Out.put(obj, "productSystem", v.getProductSystem(), refFn);
			else
				Out.put(obj, "productSystem", v.getProductSystem(), null);
			Out.put(obj, "amount", v.getAmount());
			Out.put(obj, "unit", v.getUnit(), null);
			Out.put(obj, "allocationMethod", v.getAllocationMethod());
			FlowProperty prop = null;
			if (v.getFlowPropertyFactor() != null)
				prop = v.getFlowPropertyFactor().getFlowProperty();
			Out.put(obj, "flowProperty", prop, refFn);
			ParameterRedefs.map(obj, v.getParameterRedefs(), conf.db, refFn,
					this::createRef);
			array.add(obj);
		}
		Out.put(json, "variants", array);
	}

	private JsonObject createRef(ModelType type, Long id) {
		if (id == null)
			return null;
		if (type == null)
			return null;
		if (exportReferences)
			return References.create(type, id, conf, refFn);
		return References.create(type, id, conf, null);
	}

}
