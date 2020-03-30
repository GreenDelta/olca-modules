package org.openlca.jsonld.output;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
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
		List<ParameterRedef> globalRedefs = null;

		for (ParameterRedef p : redefs) {

			JsonObject obj = new JsonObject();
			array.add(obj);
			Out.put(obj, "@type", "ParameterRedef");
			Out.put(obj, "name", p.name);
			Out.put(obj, "value", p.value);
			Out.put(obj, "uncertainty",
					Uncertainties.map(p.uncertainty));

			if (p.contextId == null) {
				// global parameter redefinition
				if (globalRedefs == null) {
					globalRedefs = new ArrayList<>();
				}
				globalRedefs.add(p);
			} else if (p.contextType != null) {
				// reference a LCIA category or process
				boolean exportIt = p.contextType == ModelType.IMPACT_CATEGORY;
				JsonObject ref = References.create(
					p.contextType, p.contextId, conf, exportIt);
				Out.put(obj, "context", ref);
			}
		}

		syncGlobals(globalRedefs, conf);
		return array;
	}

	/**
	 * Export redefined global parameters if necessary.
	 */
	private static void syncGlobals(
			List<ParameterRedef> globalRedefs, ExportConfig conf) {

		if (globalRedefs == null || globalRedefs.isEmpty())
			return;
		if (!conf.exportReferences || conf.refFn == null)
			return;

		// load all global parameters
		Map<String, Parameter> globals = new ParameterDao(conf.db)
				.getGlobalParameters()
				.stream()
				.filter(p -> p.name != null)
				.collect(Collectors.toMap(
						p -> p.name.trim().toLowerCase(),
						p -> p));

		// export the referenced global parameters
		for (ParameterRedef redef : globalRedefs) {
			if (redef.name == null)
				continue;
			Parameter global = globals.get(
					redef.name.trim().toLowerCase());
			if (global != null) {
				conf.refFn.accept(global);
			}
		}
	}
}
