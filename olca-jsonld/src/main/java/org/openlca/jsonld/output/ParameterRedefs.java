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
			IDatabase database, ExportConfig conf, RefLoader loader) {
		JsonArray array = new JsonArray();
		for (ParameterRedef p : redefs) {
			JsonObject obj = new JsonObject();
			Out.put(obj, "@type", ParameterRedef.class.getSimpleName());
			Out.put(obj, "name", p.getName(), Out.REQUIRED_FIELD);
			Out.put(obj, "value", p.getValue());
			Out.put(obj, "uncertainty", Uncertainties.map(p.getUncertainty()));
			Out.put(obj, "context",
					loader.load(p.getContextType(), p.getContextId()));
			if (p.getContextId() == null && conf.db != null) {
				Parameter global = loadParameter(database, p.getName());
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

	static interface RefLoader {

		JsonObject load(ModelType type, Long id);

	}

}
