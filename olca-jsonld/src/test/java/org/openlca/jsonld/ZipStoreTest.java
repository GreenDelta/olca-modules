package org.openlca.jsonld;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.model.ModelType;

import com.google.gson.JsonObject;

public class ZipStoreTest {

	// TODO: add parameters
	private ModelType[] types = {
			ModelType.CATEGORY, ModelType.LOCATION, ModelType.ACTOR,
			ModelType.SOURCE, ModelType.UNIT_GROUP, ModelType.FLOW_PROPERTY,
			ModelType.FLOW, ModelType.PROCESS, ModelType.IMPACT_METHOD,
			ModelType.IMPACT_CATEGORY, ModelType.NW_SET
	};

	private File tempDir;
	private File zipFile;

	@Before
	public void setUp() throws Exception {
		tempDir = Files.createTempDirectory("olca-json_").toFile();
		zipFile = new File(tempDir, "test_zip.zip");
	}

	@After
	public void tearDown() throws Exception {
		Assert.assertTrue(zipFile.delete());
		Assert.assertTrue(tempDir.delete());
	}

	@Test
	public void testWrite() throws Exception {
		Map<ModelType, String> entries = writeData();
		try (ZipStore store = ZipStore.open(zipFile)) {
			for (ModelType type : entries.keySet())
				Assert.assertTrue(store.contains(type, entries.get(type)));
		}
	}

	@Test
	public void testReadIds() throws Exception {
		Map<ModelType, String> entries = writeData();
		try (ZipStore store = ZipStore.open(zipFile)) {
			for (ModelType type : types) {
				List<String> list = store.getRefIds(type);
				Assert.assertEquals(1, list.size());
				Assert.assertEquals(entries.get(type), list.get(0));
			}
		}
	}

	@Test
	public void testRead() throws Exception {
		Map<ModelType, String> entries = writeData();
		try (ZipStore store = ZipStore.open(zipFile)) {
			for (ModelType type : entries.keySet()) {
				String id = entries.get(type);
				JsonObject obj = store.get(type, id);
				String name = obj.get("name").getAsString();
				Assert.assertEquals(type.name(), name);
			}
		}
	}

	private Map<ModelType, String> writeData() throws Exception {
		try (ZipStore store = ZipStore.open(zipFile)) {
			Map<ModelType, String> entries = new HashMap<>();
			for (ModelType type : types) {
				String refId = UUID.randomUUID().toString();
				entries.put(type, refId);
				JsonObject obj = new JsonObject();
				obj.addProperty("@id", refId);
				obj.addProperty("name", type.name());
				store.put(type, obj);
			}
			return entries;
		}
	}
}
