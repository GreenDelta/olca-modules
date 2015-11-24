package org.openlca.jsonld.output;

import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Parameter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ImpactMethodWriter extends Writer<ImpactMethod> {

	ImpactMethodWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	JsonObject write(ImpactMethod method) {
		JsonObject obj = super.write(method);
		if (obj == null)
			return null;
		Out.put(obj, "impactCategories", method.getImpactCategories(), conf, true);
		Out.put(obj, "nwSets", method.getNwSets(), conf, true);
		mapParameters(obj, method);
		return obj;
	}

	private void mapParameters(JsonObject json, ImpactMethod method) {
		JsonArray parameters = new JsonArray();
		for (Parameter p : method.getParameters()) {
			JsonObject obj = Writer.initJson();
			ParameterWriter.mapAttr(obj, p);
			parameters.add(obj);
		}
		Out.put(json, "parameters", parameters);

	}
}
