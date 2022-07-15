package org.openlca.jsonld.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.RefEntity;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ParameterRedefs {

	static List<ParameterRedef> read(JsonArray array, EntityResolver resolver) {
		if (array == null || array.size() == 0)
			return Collections.emptyList();
		var redefs = new ArrayList<ParameterRedef>();
		for (var elem : array) {
			if (!elem.isJsonObject())
				continue;
			var object = elem.getAsJsonObject();
			var p = new ParameterRedef();
			p.name = Json.getString(object, "name");
			p.description = Json.getString(object, "description");
			p.value = Json.getDouble(object, "value", 0);
			p.uncertainty = Uncertainties.read(Json
				.getObject(object, "uncertainty"));
			p.isProtected = Json.getBool(object, "isProtected", false);
			var context = Json.getObject(object, "context");
			boolean valid = setContext(context, p, resolver);
			if (valid) {
				redefs.add(p);
			}
		}
		return redefs;
	}

	private static boolean setContext(
		JsonObject context, ParameterRedef p, EntityResolver resolver) {
		if (context == null)
			return true;
		var type = Json.getString(context, "@type");
		var refId = Json.getString(context, "@id");
		RefEntity model = null;
		if ("Process".equals(type)) {
			p.contextType = ModelType.PROCESS;
			model = resolver.get(Process.class, refId);
		} else if ("ImpactMethod".equals(type)) {
			p.contextType = ModelType.IMPACT_METHOD;
			model = resolver.get(ImpactMethod.class, refId);
		}
		if (model == null)
			return false;
		p.contextId = model.id;
		return true;
	}

}
