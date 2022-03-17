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

	@Test
	public void testWriteModels() {
		Map<ModelType, String> entries = writeModels();
		with(zip -> {
			for (ModelType type : entries.keySet()) {
				var refId = entries.get(type);
				var json = zip.get(type, refId);
				Assert.assertNotNull(json);
			}
		});
	}

	@Test
	public void testReadIds() {
		Map<ModelType, String> entries = writeModels();
		with(zip -> {
			for (var type : entries.keySet()) {
				List<String> list = zip.getRefIds(type);
				Assert.assertEquals(1, list.size());
				Assert.assertEquals(entries.get(type), list.get(0));
			}
		});
	}

	@Test
	public void testReadModels() {
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

	private Map<ModelType, String> writeModels() {
		Map<ModelType, String> entries = new HashMap<>();
		with(zip -> {
			for (var type : ModelType.values()) {
				if (!type.isRoot())
					continue;
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
	public void testReadWriteData() {
		with(zip -> {
			String path = "my/super/file.txt";
			byte[] first = "first".getBytes();
			zip.put(path, first);
			Assert.assertArrayEquals(first, zip.getBytes(path));
			byte[] second = "second".getBytes();
			zip.put(path, second);
			Assert.assertArrayEquals(second, zip.getBytes(path));
		});
	}

	@Test
	public void testReadWriteDataWinStyle() {
		with(zip -> {
			String path = "my\\super\\file.txt";
			byte[] first = "first".getBytes();
			zip.put(path, first);
			Assert.assertArrayEquals(first, zip.getBytes(path));
			byte[] second = "second".getBytes();
			zip.put(path, second);
			Assert.assertArrayEquals(second, zip.getBytes(path));
		});
	}

	@Test
	public void testGetBinFiles() {
		with(zip -> {
			for (int i = 0; i < 10; i++) {
				String path = "bin/flows/abc/file_" + i + ".txt";
				byte[] data = "Content of file".getBytes();
				zip.put(path, data);
			}
			List<String> paths = zip.getBinFiles(ModelType.FLOW, "abc");
			Assert.assertEquals(10, paths.size());
			for (String path : paths) {
				byte[] data = zip.getBytes(path);
				String s = new String(data);
				Assert.assertEquals("Content of file", s);
			}
		});
	}


}
