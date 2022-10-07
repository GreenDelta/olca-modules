package org.openlca.ipc;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.openlca.jsonld.Json;

public class ModelTest {

	private static final AtomicInteger nextID = new AtomicInteger(0);

	@Test
	@Ignore
	public void testCRUD() {

		// insert a model
		var req = prepareRequest();
		req.method = "insert/model";
		Json.put(req.params.getAsJsonObject(), "name", "a flow");
		var result = Tests.post(req).result.getAsJsonObject();
		assertEquals("a flow", Json.getString(result, "name"));

		// get the model
		req.method = "get/model";
		result = Tests.post(req).result.getAsJsonObject();
		assertEquals("a flow", Json.getString(result, "name"));

		// update the model
		req.method = "update/model";
		Json.put(req.params.getAsJsonObject(), "name", "a better flow");
		result = Tests.post(req).result.getAsJsonObject();
		assertEquals("a better flow", Json.getString(result, "name"));

		// get the updated model
		req.method = "get/model";
		result = Tests.post(req).result.getAsJsonObject();
		assertEquals("a better flow", Json.getString(result, "name"));

		// delete the model
		req.method = "delete/model";
		result = Tests.post(req).result.getAsJsonObject();
		assertEquals("a better flow", Json.getString(result, "name"));

		// assert that the model does not exist
		req.method = "get/model";
		var err = Tests.post(req);
		Assert.assertEquals(404, err.error.code); // = not found
	}

	private RpcRequest prepareRequest() {
		RpcRequest req = new RpcRequest();
		req.jsonrpc = "2.0";
		req.id = new JsonPrimitive(nextID.incrementAndGet());
		JsonObject flow = new JsonObject();
		flow.addProperty("@type", "Flow");
		flow.addProperty("@id", "aFlow");
		req.params = flow;
		return req;
	}
}
