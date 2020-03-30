package org.openlca.jsonld.output;

import java.util.List;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ParameterRedefs {

	/**
	 * Maps the given parameter redefinitions to a JSON array. If necessary, it
	 * exports the respective parameter context (i.e. global parameters or LCIA
	 * category parameters).
	 */
	static JsonArray map(List<ParameterRedef> redefs, ExportConfig conf) {
		JsonArray array = new JsonArray();
		for (ParameterRedef p : redefs) {
			JsonObject obj = new JsonObject();
			array.add(obj);
			Out.put(obj, "@type", "ParameterRedef");
			Out.put(obj, "name", p.name);
			Out.put(obj, "value", p.value);
			Out.put(obj, "uncertainty",
					Uncertainties.map(p.uncertainty));
			if (p.contextType != null) {
				boolean exportIt = p.contextType == ModelType.IMPACT_CATEGORY;
				JsonObject ref = References.create(
					p.contextType, p.contextId, conf, exportIt);
				Out.put(obj, "context", ref);
			}
		}
		return array;
	}
}
