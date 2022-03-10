package org.openlca.ipc;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ModelTest {

	private static final AtomicInteger nextID = new AtomicInteger(0);

	@Test
	@Ignore
	public void testCRUD() {

		Gson gson = new Gson();

		// insert a model
		RpcRequest req = prepareRequest();
		req.method = "insert/model";
		req.params.getAsJsonObject().addProperty("name", "a flow");
		String json = Tests.post(gson.toJson(req));
		RpcResponse resp = gson.fromJson(json, RpcResponse.class);
		Assert.assertEquals("ok", resp.result.getAsString());

		// get the model
		req.method = "get/model";
		json = Tests.post(gson.toJson(req));
		resp = gson.fromJson(json, RpcResponse.class);
		JsonObject flow = resp.result.getAsJsonObject();
		Assert.assertEquals("a flow", flow.get("name").getAsString());

		// update the model
		req.method = "update/model";
		req.params.getAsJsonObject().addProperty("name", "a better flow");
		json = Tests.post(gson.toJson(req));
		resp = gson.fromJson(json, RpcResponse.class);
		Assert.assertEquals("ok", resp.result.getAsString());

		// get the updated model
		req.method = "get/model";
		json = Tests.post(gson.toJson(req));
		resp = gson.fromJson(json, RpcResponse.class);
		flow = resp.result.getAsJsonObject();
		Assert.assertEquals("a better flow", flow.get("name").getAsString());

		// delete the model
		req.method = "delete/model";
		json = Tests.post(gson.toJson(req));
		resp = gson.fromJson(json, RpcResponse.class);
		Assert.assertEquals("ok", resp.result.getAsString());

		// assert that the model does not exist
		req.method = "get/model";
		json = Tests.post(gson.toJson(req));
		resp = gson.fromJson(json, RpcResponse.class);
		Assert.assertEquals(404, resp.error.code); // = not found
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
