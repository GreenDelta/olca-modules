package org.openlca.cloud.api;

import java.util.ArrayList;
import java.util.List;

import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

class ListRepositoriesInvocation {
	
	private static final String PATH = "/repository?page=0";
	String baseUrl;
	String sessionId;

	List<String> execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		ClientResponse response = WebRequests.call(Type.GET, baseUrl + PATH, sessionId);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return null;
		JsonObject result = new Gson().fromJson(response.getEntity(String.class), JsonObject.class);
		JsonArray data = result.get("data").getAsJsonArray();
		List<String> repositories = new ArrayList<>();
		for (JsonElement e : data) {
			JsonObject repository = e.getAsJsonObject();
			String group = repository.get("group").getAsString();
			String name = repository.get("name").getAsString();
			repositories.add(group + "/" + name);
		}
		return repositories;
	}

}
