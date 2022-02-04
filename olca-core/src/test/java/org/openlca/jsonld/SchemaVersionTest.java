package org.openlca.jsonld;

import static org.junit.Assert.*;

import com.google.gson.JsonObject;
import org.junit.Test;
import org.openlca.core.model.ModelType;

import java.io.IOException;
import java.nio.file.Files;

public class SchemaVersionTest {

	@Test
	public void testCurrentZip() throws IOException  {
		var file = Files.createTempFile("_olca", ".zip");
		Files.delete(file);
		try(var store = ZipStore.open(file.toFile())) {
			init(store);
		}
		try (var store = ZipStore.open(file.toFile())) {
			check(store);
		}
		Files.delete(file);
	}

	@Test
	public void testCurrentMemStore() {
		var store = new MemStore();
		init(store);
		check(store);
	}

	private void init(JsonStoreWriter store) {
		var obj = new JsonObject();
		obj.addProperty("@id", "123");
		store.put("actors/123.json", obj);
	}

	private void check(JsonStoreReader store) {
		var version = SchemaVersion.of(store);
		assertTrue(version.isCurrent());
		var actor = store.get(ModelType.ACTOR, "123");
		assertEquals("123", Json.getString(actor, "@id"));
	}
}
