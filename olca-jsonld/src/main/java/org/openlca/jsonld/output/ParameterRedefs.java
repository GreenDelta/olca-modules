package org.openlca.jsonld.output;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterScope;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ParameterRedefs {

	static void map(JsonObject json, List<ParameterRedef> redefs,
			ExportConfig conf) {
		JsonArray array = new JsonArray();
		for (ParameterRedef p : redefs) {
			JsonObject obj = new JsonObject();
			Out.put(obj, "@type", ParameterRedef.class.getSimpleName());
			Out.put(obj, "name", p.name, Out.REQUIRED_FIELD);
			Out.put(obj, "value", p.value);
			Out.put(obj, "uncertainty", Uncertainties.map(p.uncertainty));
			Out.put(obj, "context",
					loader.load(p.contextType, p.contextId));
			if (p.contextId == null && conf.db != null) {
				Parameter global = loadParameter(conf.db, p.name);
				if (conf.exportReferences && conf.refFn != null)
					conf.refFn.accept(global);
			}
			array.add(obj);
		}
		Out.put(json, "parameterRedefs", array);
	}

	private static Parameter loadParameter(IDatabase database, String name) {
		String jpql = "SELECT p FROM Parameter p WHERE p.scope = :scope AND p.name = :name";
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("name", name);
		parameters.put("scope", ParameterScope.GLOBAL);
		return new ParameterDao(database).getFirst(jpql, parameters);
	}

}
