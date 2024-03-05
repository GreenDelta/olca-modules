package org.openlca.jsonld.upgrades;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.PackageInfo;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.ActorWriter;

import java.util.Objects;

public class Upgrade3Test {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testUpgradeReviews() {

		// create a store with schema-version = 2
		var store = new MemStore() {
			@Override
			public JsonElement getJson(String path) {
				if (Objects.equals(path, PackageInfo.FILE_NAME)) {
					var info = new JsonObject();
					Json.put(info, "schemaVersion", 2);
					return info;
				}
				return super.getJson(path);
			}
		};

		// add a process with old review data format
		var actor = Actor.of("Reviewer X");
		actor.refId = "3a1d0fa8-9f8a-4018-81a4-7b609a981a2c";
		store.put(ModelType.ACTOR, new ActorWriter().write(actor));

		var data = """
				{
					"@type":"Process",
					"@id":"24c330b5-f4d8-4ae1-8f44-ec0f08ef64d2",
					"name":"P",
					"processDocumentation":{
				     "reviewDetails":"some review details",
				     "reviewer":{
				       "@type":"Actor",
				       "@id":"3a1d0fa8-9f8a-4018-81a4-7b609a981a2c",
				       "name":"Reviewer X"
				     }
				  }
				}
				""";
		store.put(
				ModelType.PROCESS,
				new Gson().fromJson(data, JsonObject.class));

		// run the import and check
		new JsonImport(store, db).run();
		var process = db.get(
				Process.class, "24c330b5-f4d8-4ae1-8f44-ec0f08ef64d2");
		var doc = process.documentation;
		assertEquals(1, doc.reviews.size());
		var rev = doc.reviews.get(0);
		assertEquals("some review details", rev.details);
		assertEquals(1, rev.reviewers.size());
		var reviewer = rev.reviewers.get(0);
		assertEquals("Reviewer X", reviewer.name);

		db.delete(process, reviewer);
	}

}
