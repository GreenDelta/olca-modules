package org.openlca.jsonld.output;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.ZipStore;
import org.openlca.util.Dirs;

import com.google.gson.JsonObject;

public class JsonExportTest {

	@Test
	public void testToString() {
		Actor actor = new Actor();
		actor.refId = UUID.randomUUID().toString();
		actor.name = "actor";
		JsonObject obj = JsonExport.toJson(actor);
		Assert.assertEquals(actor.refId, obj.get("@id").getAsString());
		Assert.assertEquals(actor.name, obj.get("name").getAsString());
	}

	@Test
	public void testWriteActor() throws Exception {
		Actor actor = new Actor();
		actor.refId = UUID.randomUUID().toString();
		actor.name = "actor";
		Category cat = new Category();
		cat.refId = UUID.randomUUID().toString();
		cat.name = "category";
		actor.category = cat;
		Path tempdir = Files.createTempDirectory("_olca_tests_");
		Path zip = tempdir.resolve("test.zip");
		ZipStore store = ZipStore.open(zip.toFile());
		JsonExport export = new JsonExport(Tests.getDb(), store);
		AtomicInteger count = new AtomicInteger(0);
		export.write(actor, (message, entity) -> {
			count.incrementAndGet();
		});
		Assert.assertEquals(2, count.get());
		Assert.assertNotNull(store.get(ModelType.ACTOR, actor.refId));
		Assert.assertNotNull(store.get(ModelType.CATEGORY, cat.refId));
		store.close();
		Dirs.delete(tempdir);
	}

}
