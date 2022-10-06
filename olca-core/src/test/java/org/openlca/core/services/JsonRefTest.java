package org.openlca.core.services;

import static org.junit.Assert.*;

import java.util.UUID;

import com.google.gson.JsonObject;
import org.junit.Test;
import org.openlca.core.model.ModelType;

public class JsonRefTest {

	@Test
	public void testDescriptorTypes() {
		for (var type : ModelType.values()) {
			var id = UUID.randomUUID().toString();
			var name = id + "_name";
			var json = new JsonObject();
			json.addProperty("@id", id);
			json.addProperty("name", name);
			json.addProperty("@type", type.getModelClass().getSimpleName());

			assertEquals(id, JsonRef.idOf(json));
			assertEquals(type, JsonRef.typeOf(json));
			var d = JsonRef.descriptorOf(json);
			assertNotNull(d);
			assertEquals(type, d.type);
			assertEquals(id, d.refId);
			assertEquals(name, d.name);
		}
	}
}
