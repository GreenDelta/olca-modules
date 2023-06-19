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
		req.method = "data/put";
		Json.put(req.params.getAsJsonObject(), "name", "a flow");
		var result = Tests.post(req).result.getAsJsonObject();
		assertEquals("a flow", Json.getString(result, "name"));

		// get the model
		req.method = "data/get";
		result = Tests.post(req).result.getAsJsonObject();
		assertEquals("a flow", Json.getString(result, "name"));

		// update the model
		req.method = "data/put";
		Json.put(req.params.getAsJsonObject(), "name", "a better flow");
		result = Tests.post(req).result.getAsJsonObject();
		assertEquals("a better flow", Json.getString(result, "name"));

		// get the updated model
		req.method = "data/get";
		result = Tests.post(req).result.getAsJsonObject();
		assertEquals("a better flow", Json.getString(result, "name"));

		// delete the model
		req.method = "data/delete";
		result = Tests.post(req).result.getAsJsonObject();
		assertEquals("a better flow", Json.getString(result, "name"));

		// assert that the model does not exist
		req.method = "data/get";
		var err = Tests.post(req);
		Assert.assertEquals(404, err.error.code); // = not found
	}

	private RpcRequest prepareRequest() {
		var req = new RpcRequest();
		req.jsonrpc = "2.0";
		req.id = new JsonPrimitive(nextID.incrementAndGet());
		var flow = new JsonObject();
		flow.addProperty("@type", "Flow");
		flow.addProperty("@id", "aFlow");
		req.params = flow;
		return req;
	}
}
