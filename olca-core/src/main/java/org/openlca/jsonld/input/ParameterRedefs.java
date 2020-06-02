package org.openlca.jsonld.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ParameterRedefs {

	static List<ParameterRedef> read(JsonArray array, ImportConfig conf) {
		if (array == null || array.size() == 0)
			return Collections.emptyList();
		List<ParameterRedef> redefs = new ArrayList<>();
		for (JsonElement elem : array) {
			if (!elem.isJsonObject())
				continue;
			JsonObject ref = elem.getAsJsonObject();
			ParameterRedef p = new ParameterRedef();
			p.name = Json.getString(ref, "name");
			p.description = Json.getString(ref, "description");
			p.value = Json.getDouble(ref, "value", 0);
			p.uncertainty = Uncertainties.read(Json
					.getObject(ref, "uncertainty"));
			JsonObject context = Json.getObject(ref, "context");
			boolean valid = setContext(context, p, conf);
			if (valid) {
				redefs.add(p);
			}
		}
		return redefs;
	}

	private static boolean setContext(
			JsonObject context, ParameterRedef p, ImportConfig conf) {
		if (context == null)
			return true;
		String type = Json.getString(context, "@type");
		String refId = Json.getString(context, "@id");
		RootEntity model = null;
		if ("Process".equals(type)) {
			p.contextType = ModelType.PROCESS;
			model = ProcessImport.run(refId, conf);
		} else if ("ImpactMethod".equals(type)) {
			p.contextType = ModelType.IMPACT_METHOD;
			model = ImpactMethodImport.run(refId, conf);
		}
		if (model == null)
			return false;
		p.contextId = model.id;
		return true;
	}

}
