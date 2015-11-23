package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.RootEntity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ImpactMethodWriter extends Writer<ImpactMethod> {

	@Override
	JsonObject write(ImpactMethod method, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(method, refFn);
		if (obj == null)
			return null;
		Out.put(obj, "impactCategories", method.getImpactCategories(), refFn);
		Out.put(obj, "nwSets", method.getNwSets(), refFn);
		mapParameters(obj, method);
		return obj;
	}

	private void mapParameters(JsonObject json, ImpactMethod method) {
		JsonArray parameters = new JsonArray();
		for (Parameter p : method.getParameters()) {
			JsonObject obj = new ParameterWriter().write(p, ref -> {
			});
			parameters.add(obj);
		}
		Out.put(json, "parameters", parameters);

	}
}
