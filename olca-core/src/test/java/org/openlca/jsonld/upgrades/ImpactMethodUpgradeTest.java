package org.openlca.jsonld.upgrades;

import java.util.function.Consumer;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterScope;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.PackageInfo;
import org.openlca.jsonld.SchemaVersion;
import org.openlca.jsonld.input.JsonImport;

public class ImpactMethodUpgradeTest {

	private final IDatabase db = Tests.getDb();
	private final MemStore store = new MemStore();

	@Before
	public void setup() {
		store.put(PackageInfo.FILE_NAME,
			PackageInfo.create()
				.withSchemaVersion(SchemaVersion.fallback())
				.json());
		store.put(ModelType.IMPACT_METHOD, json("""
			{ "@id": "m1",
			  "name": "Method1",
			  "impactCategories": [	{"@id": "i1" }, {"@id": "i2" } ],
			  "nwSets": [	{"@id": "nw1" } ],
				"parameters": [
					{ "@id": "p1",
						"name": "param",
						"inputParameter": true,
						"value": 38.0
					}]}"""));
		store.put(ModelType.IMPACT_CATEGORY, json("""
			{ "@id": "i1", "name": "impact1" }"""));
		store.put(ModelType.IMPACT_CATEGORY, json("""
			{ "@id": "i2", "name": "impact2" }"""));

		store.put("nw_sets/nw1.json", json("""
			{
				"@id": "nw1",
				"name": "nw1",
				"weightedScoreUnit": "u",
				"factors": [
					{
						"impactCategory": { "@id": "i1" },
						"normalisationFactor": 1.0,
						"weightingFactor": 1.0
					},
					{
						"impactCategory": { "@id": "i2" },
						"normalisationFactor": 2.0,
						"weightingFactor": 2.0
					}
				]
			}
			"""));

		new JsonImport(store, db).run();
	}

	private JsonObject json(String s) {
		return new Gson().fromJson(s, JsonObject.class);
	}

	@After
	public void cleanup() {
		db.clear();
		store.clear();
	}

	@Test
	public void testCopyParameters() {
		Consumer<String> check = id -> {
			var impact = db.get(ImpactCategory.class, id);
			assertNotNull(impact);
			var param = impact.parameters.get(0);
			assertEquals("param", param.name);
			assertEquals(ParameterScope.IMPACT, param.scope);
			assertEquals(38, param.value, 0);
			assertTrue(param.isInputParameter);
		};
		check.accept("i1");
		check.accept("i2");
	}

	@Test
	public void testInlinedNwSets() {
		var method = db.get(ImpactMethod.class, "m1");
		var nwSet = method.nwSets.get(0);
		assertEquals("nw1", nwSet.name);
		var i1 = db.get(ImpactCategory.class, "i1");
		var i2 = db.get(ImpactCategory.class, "i2");
		var f1 = nwSet.getFactor(i1);
		var f2 = nwSet.getFactor(i2);
		assertEquals(1, f1.normalisationFactor, 0);
		assertEquals(1, f1.weightingFactor, 0);
		assertEquals(2, f2.normalisationFactor, 0);
		assertEquals(2, f2.weightingFactor, 0);
	}

}
