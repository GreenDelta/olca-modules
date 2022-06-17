package org.openlca.jsonld.output;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.ZipStore;
import org.openlca.util.Dirs;

public class JsonExportTest {

	@Test
	public void testToString() {
		var actor = Actor.of("actor");
		var obj = JsonExport.toJson(actor);
		assertEquals(actor.refId, obj.get("@id").getAsString());
		assertEquals(actor.name, obj.get("name").getAsString());
	}

	@Test
	public void testWriteActor() throws Exception {
		var actor = Actor.of("actor");
		actor.category = Category.of("experts", ModelType.ACTOR);

		var count = new AtomicInteger(0);
		var tempDir = Files.createTempDirectory("_olca_tests_").toFile();
		var zip = new File(tempDir, "test.zip");
		try (var store = ZipStore.open(zip)){
			var export = new JsonExport(Tests.getDb(), store);
			export.write(actor, ($, e) -> count.incrementAndGet());
		}
		assertEquals(1, count.get());

		try (var store = ZipStore.open(zip)) {
			var json = store.get(ModelType.ACTOR, actor.refId);
			assertNotNull(json);
			assertEquals("experts", Json.getString(json, "category"));
		}
		Dirs.delete(tempDir);
	}
}
