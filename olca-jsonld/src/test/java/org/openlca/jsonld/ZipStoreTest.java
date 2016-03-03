package org.openlca.jsonld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.model.ModelType;

import com.google.gson.JsonObject;

public class ZipStoreTest extends AbstractZipTest {

	private ModelType[] types = {
			ModelType.CATEGORY, ModelType.LOCATION, ModelType.ACTOR,
			ModelType.SOURCE, ModelType.UNIT_GROUP, ModelType.FLOW_PROPERTY,
			ModelType.FLOW, ModelType.PROCESS, ModelType.IMPACT_METHOD,
			ModelType.IMPACT_CATEGORY, ModelType.NW_SET, ModelType.PARAMETER,
			ModelType.SOCIAL_INDICATOR
	};

	@Test
	public void testWriteModels() throws Exception {
		Map<ModelType, String> entries = writeModels();
		with(zip -> {
			for (ModelType type : entries.keySet())
				Assert.assertTrue(zip.contains(type, entries.get(type)));
		});
	}

	@Test
	public void testReadIds() throws Exception {
		Map<ModelType, String> entries = writeModels();
		with(zip -> {
			for (ModelType type : types) {
				List<String> list = zip.getRefIds(type);
				Assert.assertEquals(1, list.size());
				Assert.assertEquals(entries.get(type), list.get(0));
			}
		});
	}

	@Test
	public void testReadModels() throws Exception {
		Map<ModelType, String> entries = writeModels();
		with(zip -> {
			for (ModelType type : entries.keySet()) {
				String id = entries.get(type);
				JsonObject obj = zip.get(type, id);
				String name = obj.get("name").getAsString();
				Assert.assertEquals(type.name(), name);
			}
		});
	}

	private Map<ModelType, String> writeModels() throws Exception {
		Map<ModelType, String> entries = new HashMap<>();
		with(zip -> {
			for (ModelType type : types) {
				String refId = UUID.randomUUID().toString();
				entries.put(type, refId);
				JsonObject obj = new JsonObject();
				obj.addProperty("@id", refId);
				obj.addProperty("name", type.name());
				zip.put(type, obj);
			}
		});
		return entries;
	}

	@Test
	public void testReadWriteData() throws Exception {
		with(zip -> {
			String path = "my/super/file.txt";
			byte[] first = "first".getBytes();
			zip.put(path, first);
			Assert.assertArrayEquals(first, zip.get(path));
			byte[] second = "second".getBytes();
			zip.put(path, second);
			Assert.assertArrayEquals(second, zip.get(path));
		});
	}

	@Test
	public void testReadWriteDataWinStyle() throws Exception {
		with(zip -> {
			String path = "my\\super\\file.txt";
			byte[] first = "first".getBytes();
			zip.put(path, first);
			Assert.assertArrayEquals(first, zip.get(path));
			byte[] second = "second".getBytes();
			zip.put(path, second);
			Assert.assertArrayEquals(second, zip.get(path));
		});
	}

	@Test
	public void testGetBinFiles() throws Exception {
		with(zip -> {
			for (int i = 0; i < 10; i++) {
				String path = "bin/flows/abc/file_" + i + ".txt";
				byte[] data = "Content of file".getBytes();
				zip.put(path, data);
			}
			List<String> paths = zip.getBinFiles(ModelType.FLOW, "abc");
			Assert.assertEquals(10, paths.size());
			for (String path : paths) {
				byte[] data = zip.get(path);
				String s = new String(data);
				Assert.assertEquals("Content of file", s);
			}
		});
	}

}
