package org.openlca.jsonld;

import static org.junit.Assert.*;

import com.google.gson.JsonObject;
import org.junit.Test;
import org.openlca.core.model.ModelType;
import org.openlca.util.Dirs;

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

	@Test
	public void testCurrentFileStore() throws IOException {
		var dir = Files.createTempDirectory("_olca").toFile();
		var writer = new FileStoreWriter(dir);
		init(writer);
		var reader = new FileStoreReader(dir);
		check(reader);
		Dirs.delete(dir);
	}

	private void init(JsonStoreWriter store) {
		var obj = new JsonObject();
		obj.addProperty("@id", "123");
		store.put("actors/123.json", obj);
	}

	private void check(JsonStoreReader store) {
		var version = PackageInfo.readFrom(store)
			.schemaVersion();
		assertTrue(version.isCurrent());
		var actor = store.get(ModelType.ACTOR, "123");
		assertEquals("123", Json.getString(actor, "@id"));
	}
}
