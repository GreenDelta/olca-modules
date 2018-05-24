package org.openlca.jsonld.input;

import java.util.List;

import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ParameterRedefs {

	static void addParameters(JsonObject json, List<ParameterRedef> list,
			ImportConfig conf) {
		JsonArray array = Json.getArray(json, "parameterRedefs");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement element : array) {
			JsonObject ref = element.getAsJsonObject();
			ParameterRedef p = new ParameterRedef();
			p.setName(Json.getString(ref, "name"));
			p.setValue(Json.getDouble(ref, "value", 0));
			p.setUncertainty(Uncertainties.read(Json
					.getObject(ref, "uncertainty")));
			JsonObject context = Json.getObject(ref, "context");
			boolean valid = setContext(context, p, conf);
			if (valid)
				list.add(p);
		}
	}

	private static boolean setContext(JsonObject context, ParameterRedef p,
			ImportConfig conf) {
		if (context == null)
			return true;
		String type = Json.getString(context, "@type");
		String refId = Json.getString(context, "@id");
		RootEntity model = null;
		if (Process.class.getSimpleName().equals(type)) {
			model = ProcessImport.run(refId, conf);
			if (model == null)
				return false;
			p.setContextType(ModelType.PROCESS);
		} else if (ImpactMethod.class.getSimpleName().equals(type)) {
			model = ImpactMethodImport.run(refId, conf);
			if (model == null)
				return false;
			p.setContextType(ModelType.IMPACT_METHOD);
		}
		p.setContextId(model.getId());
		return true;
	}

}
