package org.openlca.jsonld.input;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.MemStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CategoryPathTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testSyncCategoryPaths() {
		var json = """
			{
				"@type": "Actor",
				"@id": "my-actor-1",
				"name": "My actor",
				"category": "some/category/path"
			}
			""";
		var memStore = new MemStore();
		memStore.put(ModelType.ACTOR, new Gson().fromJson(json, JsonObject.class));
		new JsonImport(memStore, db).run();
		var actor = db.get(Actor.class, "my-actor-1");
		assertNotNull(actor);
		assertNotNull(actor.category);
		assertEquals("some/category/path", actor.category.toPath());
		db.clear();
	}
}
