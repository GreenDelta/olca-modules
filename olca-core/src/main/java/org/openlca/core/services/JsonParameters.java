package org.openlca.core.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterizedEntity;
import org.openlca.core.model.ProductSystem;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.jsonld.output.ParameterWriter;
import org.openlca.util.ParameterRedefSets;

import com.google.gson.JsonArray;

class JsonParameters {

	static JsonArray of(JsonExport exp, ParameterizedEntity entity) {
		var array = new JsonArray();
		if (entity == null || entity.parameters.isEmpty())
			return array;
		for (var param : entity.parameters) {
			var json = new ParameterWriter(exp).write(param);
			array.add(json);
		}
		return array;
	}

	static JsonArray of(IDatabase db, ProductSystem system) {

		// get the baseline parameter values
		var baseline = system.parameterSets.stream()
				.filter(s -> s.isBaseline)
				.findAny()
				.map(s -> s.parameters.stream().collect(
						Collectors.toMap(JsonParameters::keyOf, p -> p, (p1, p2) -> p1)))
				.orElse(Collections.emptyMap());

		// get all possible redefinitions with default values
		var all = ParameterRedefSets.allOf(db, system).parameters;
		var exp = new JsonExport(db, new MemStore())
				.withReferences(false);
		if (baseline.isEmpty())
			return ParameterWriter.mapRedefs(exp, all);

		// merge the baseline values with the other redefinitions
		var merged = new ArrayList<ParameterRedef>(all.size());
		for (var redef : all) {
			var base = baseline.get(keyOf(redef));
			if (base != null) {
				merged.add(base);
			} else {
				merged.add(redef);
			}
		}
		return ParameterWriter.mapRedefs(exp, merged);
	}

	private static String keyOf(ParameterRedef redef) {
		if (redef == null || redef.name == null)
			return "";
		return redef.contextType != null
				? redef.name + "/" + redef.contextType + "/" + redef.contextId
				: redef.name;
	}

	// TODO: project parameters: ->* variants ->* parameter redefinitions

}
