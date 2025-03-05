package org.openlca.core.services;

import static org.junit.Assert.*;

import java.util.UUID;

import com.google.gson.JsonObject;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;

public class JsonDataServiceTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testCrud() {

		var service = new JsonDataService(db, db.getDataPackages());

		for (var type : ModelType.values()) {
			if (type == ModelType.CATEGORY)
				continue;

			var id = UUID.randomUUID().toString();
			var name = id + "_name";
			var json = new JsonObject();
			Json.put(json, "@id", id);
			Json.put(json, "name", name);
			Json.put(json, "@type", type.getModelClass().getSimpleName());

			// create
			var r = service.put(json);
			assertTrue(r.isValue());
			assertEquals(id, JsonRef.idOf(r.value()));

			// read
			r = service.get(type.getModelClass(), id);
			assertTrue(r.isValue());
			assertEquals(id, JsonRef.idOf(r.value()));
			assertEquals(name, Json.getString(r.value(), "name"));
			r = service.getDescriptor(type.getModelClass(), id);
			assertTrue(r.isValue());
			assertEquals(id, JsonRef.idOf(r.value()));
			assertEquals(name, Json.getString(r.value(), "name"));

			// update
			var name2 = name + "2";
			Json.put(json, "name", name2);
			service.put(json);
			r = service.get(type.getModelClass(), id);
			assertTrue(r.isValue());
			assertEquals(name2, Json.getString(r.value(), "name"));
			r = service.getDescriptor(type.getModelClass(), id);
			assertTrue(r.isValue());
			assertEquals(name2, Json.getString(r.value(), "name"));

			// delete
			r = service.delete(type.getModelClass(), id);
			assertTrue(r.isValue());
			assertEquals(id, JsonRef.idOf(r.value()));
			r = service.delete(type.getModelClass(), id);
			assertTrue(r.isEmpty());
			r = service.get(type.getModelClass(), id);
			assertTrue(r.isEmpty());
		}
	}

}
