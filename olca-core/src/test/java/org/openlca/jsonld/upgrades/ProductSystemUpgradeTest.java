package org.openlca.jsonld.upgrades;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.PackageInfo;
import org.openlca.jsonld.SchemaVersion;
import org.openlca.jsonld.input.JsonImport;

public class ProductSystemUpgradeTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testCreateParameterSets() {
		var store = new MemStore();
		store.put(PackageInfo.FILE_NAME,
			PackageInfo.create()
				.withSchemaVersion(SchemaVersion.fallback())
				.json());

		store.put(ModelType.PRODUCT_SYSTEM, json("""
			{"@id": "sys1",
			"parameterRedefs": [
				{"name": "global_param", "value": 42.0}
			]}
			"""));
		new JsonImport(store, db).run();

		var sys = db.get(ProductSystem.class, "sys1");
		assertNotNull(sys);
		var set = sys	.parameterSets.get(0);
		var redef = set.parameters.get(0);
		assertEquals("global_param", redef.name);
		assertEquals(42, redef.value, 0);
		assertNull(redef.contextId);
	}

	private JsonObject json(String s) {
		return new Gson().fromJson(s, JsonObject.class);
	}

}
