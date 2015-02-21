package org.openlca.jsonld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.model.ModelType;

import com.google.gson.JsonObject;

public class DocumentTest {

	// TODO: add parameters
	private ModelType[] types = {
			ModelType.CATEGORY, ModelType.LOCATION, ModelType.ACTOR,
			ModelType.SOURCE, ModelType.UNIT_GROUP, ModelType.FLOW_PROPERTY,
			ModelType.FLOW, ModelType.PROCESS, ModelType.IMPACT_METHOD,
			ModelType.IMPACT_CATEGORY, ModelType.NW_SET
	};

	@Test
	public void testWrite() throws Exception {
		Document doc = new Document();
		Map<ModelType, String> entries = writeData(doc);
		for (ModelType type : entries.keySet())
			Assert.assertTrue(doc.contains(type, entries.get(type)));
	}

	@Test
	public void testReadIds() throws Exception {
		Document doc = new Document();
		Map<ModelType, String> entries = writeData(doc);
		for (ModelType type : types) {
			List<String> list = doc.getRefIds(type);
			Assert.assertEquals(1, list.size());
			Assert.assertEquals(entries.get(type), list.get(0));
		}
	}

	@Test
	public void testRead() throws Exception {
		Document doc = new Document();
		Map<ModelType, String> entries = writeData(doc);
		for (ModelType type : entries.keySet()) {
			String id = entries.get(type);
			JsonObject obj = doc.get(type, id);
			String name = obj.get("name").getAsString();
			Assert.assertEquals(type.name(), name);
		}
	}

	private Map<ModelType, String> writeData(Document doc) throws Exception {
		Map<ModelType, String> entries = new HashMap<>();
		for (ModelType type : types) {
			String refId = UUID.randomUUID().toString();
			entries.put(type, refId);
			JsonObject obj = new JsonObject();
			obj.addProperty("@id", refId);
			obj.addProperty("name", type.name());
			doc.put(type, obj);
		}
		return entries;
	}
}
