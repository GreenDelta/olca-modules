package org.openlca.jsonld.input;

import java.util.List;

import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ParameterRedefs {

	static void addParameters(JsonObject json, List<ParameterRedef> list,
			ImportConfig conf) {
		JsonArray array = In.getArray(json, "parameterRedefs");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement element : array) {
			JsonObject ref = element.getAsJsonObject();
			ParameterRedef p = new ParameterRedef();
			p.setName(In.getString(ref, "name"));
			p.setValue(In.getDouble(ref, "value", 0));
			p.setUncertainty(Uncertainties.read(In
					.getObject(ref, "uncertainty")));
			JsonObject context = In.getObject(ref, "context");
			boolean valid = setContext(context, p, conf);
			if (valid)
				list.add(p);
		}
	}

	private static boolean setContext(JsonObject context, ParameterRedef p,
			ImportConfig conf) {
		if (context == null)
			return true;
		String type = In.getString(context, "@type");
		String refId = In.getString(context, "@id");
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
