package org.openlca.jsonld.upgrades;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.PackageInfo;
import org.openlca.jsonld.SchemaVersion;

public class Upgrade2Test {

	private final MemStore store = new MemStore();

	@Before
	public void setup() {
		PackageInfo.create()
			.withSchemaVersion(SchemaVersion.fallback())
			.writeTo(store);
	}

	@Test
	public void testCurrency() {
		store.put(ModelType.CURRENCY, json("""
			{	"@id": "123",
			  "referenceCurrency": {"@id": "234"} }
			"""));
		var upgrades = Upgrades.chain(store);
		var obj = upgrades.get(ModelType.CURRENCY, "123");
		var refId = Json.getRefId(obj, "refCurrency");
		assertEquals("234", refId);
	}

	@Test
	public void testProductSystem() {
		store.put(ModelType.PRODUCT_SYSTEM, json("""
			{	"@id": "123",
			  "referenceProcess": {"@id": "proc_234" },
			  "referenceExchange": {"someId": 22} }
			"""));
		var upgrades = Upgrades.chain(store);
		var obj = upgrades.get(ModelType.PRODUCT_SYSTEM, "123");
		var refId = Json.getRefId(obj, "refProcess");
		assertEquals("proc_234", refId);
		var refEx = Json.getObject(obj, "refExchange");
		assertEquals(22, Json.getInt(refEx, "someId", 0));
	}

	private static JsonObject json(String s) {
		return new Gson().fromJson(s, JsonObject.class);
	}

}
