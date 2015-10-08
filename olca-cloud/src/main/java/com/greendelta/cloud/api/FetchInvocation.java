package com.greendelta.cloud.api;

import java.util.List;
import java.util.Map;

import org.openlca.core.model.ModelType;
import org.openlca.jsonld.EntityStore;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.greendelta.cloud.model.data.FetchResponse;
import com.greendelta.cloud.util.Strings;
import com.greendelta.cloud.util.Valid;
import com.greendelta.cloud.util.WebRequests;
import com.greendelta.cloud.util.WebRequests.Type;
import com.greendelta.cloud.util.WebRequests.WebRequestException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Invokes a web service call to fetch the latest changes after the specified
 * commit id
 */
class FetchInvocation {

	private static final String PATH = "/repository/fetch/";

	private String baseUrl;
	private String sessionId;
	private String repositoryId;
	private String latestCommitId;
	private EntityStore store;

	public FetchInvocation(EntityStore store) {
		this.store = store;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public void setLatestCommitId(String latestCommitId) {
		this.latestCommitId = latestCommitId;
	}

	/**
	 * Retrieves all changed data sets since the last fetch
	 * 
	 * @return The latest commit id
	 * @throws WebRequestException
	 *             If user is out of sync or has no access to the specified
	 *             repository
	 */
	public String execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (latestCommitId == null || latestCommitId.isEmpty())
			latestCommitId = "null";
		String url = Strings.concat(baseUrl, PATH, repositoryId, "/",
				latestCommitId);
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return null;
		FetchResponse result = new Gson().fromJson(
				response.getEntity(String.class), FetchResponse.class);
		putInStore(result.getData());
		return result.getLatestCommitId();
	}

	private void putInStore(Map<ModelType, List<String>> input) {
		for (ModelType type : input.keySet()) {
			for (String json : input.get(type)) {
				JsonElement element = new Gson().fromJson(json,
						JsonElement.class);
				store.put(type, element.getAsJsonObject());
			}
		}
	}

}
