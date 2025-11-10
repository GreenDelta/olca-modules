package org.openlca.core.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public class AdditionalPropertiesTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testCrud() throws Exception {
		for (var t : ModelType.values()) {

			// write with props
			var type = t.getModelClass();
			var e1 = type.getConstructor().newInstance();
			var props1 = new JsonObject();
			Json.put(props1, "@type", type.getSimpleName());
			e1.writeOtherProperties(props1);
			db.insert(e1);

			// read and update
			var e2 = db.get(type, e1.id);
			var props2 = e2.readOtherProperties();
			assertEquals(type.getSimpleName(), Json.getString(props2, "@type"));
			Json.put(props2, "name", t.name());
			e2.writeOtherProperties(props2);
			var e3 = db.update(e2);
			var props3 = e3.readOtherProperties();
			assertEquals(type.getSimpleName(), Json.getString(props3, "@type"));
			assertEquals(t.name(), Json.getString(props3, "name"));

			// delete
			e3.writeOtherProperties(null);
			var e4 = db.update(e3);
			var props4 = e4.readOtherProperties();
			assertNull(Json.getString(props4, "@type"));
			assertNull(Json.getString(props4, "name"));
			db.delete(e4);
		}
	}
}
