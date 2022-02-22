package org.openlca.jsonld.upgrades;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.After;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterScope;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.SchemaVersion;
import org.openlca.jsonld.input.JsonImport;

public class ImpactMethodUpgradeTest {

	private final IDatabase db = Tests.getDb();

	@After
	public void cleanup() {
		db.clear();
	}

	@Test
	public void testCopyParameters() {
		var store = new MemStore();
		store.put(SchemaVersion.FILE_NAME, new SchemaVersion(1).toJson());
		store.put(ModelType.IMPACT_METHOD, json("""
			{ "@id": "m1",
			  "name": "Method1",
			  "impactCategories": [	{"@id": "i1" }, {"@id": "i2" } ],
				"parameters": [
					{ "@id": "p1",
						"name": "param",
						"inputParameter": true,
						"value": 38.0
					}
				]}
			"""));
		store.put(ModelType.IMPACT_CATEGORY, json("""
			{ "@id": "i1", "name": "impact1" }
			"""));
		store.put(ModelType.IMPACT_CATEGORY, json("""
			{ "@id": "i2", "name": "impact2" }
			"""));

		new JsonImport(store, db).run();

		var impact1 = db.get(ImpactCategory.class, "i1");
		assertNotNull(impact1);

		var param = impact1.parameters.get(0);
		assertEquals("param", param.name);
		assertEquals(ParameterScope.IMPACT, param.scope);
		assertEquals(38, param.value, 0);

	}

	private JsonObject json(String s) {
		return new Gson().fromJson(s, JsonObject.class);
	}

}
