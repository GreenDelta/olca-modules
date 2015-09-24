package com.greendelta.cloud.api;

import org.openlca.core.model.ModelType;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.greendelta.cloud.util.Strings;
import com.greendelta.cloud.util.Valid;
import com.greendelta.cloud.util.WebRequests;
import com.greendelta.cloud.util.WebRequests.Type;
import com.greendelta.cloud.util.WebRequests.WebRequestException;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Invokes a web service call to load a data set matching by type, refId and
 * commitId
 */
class DatasetContentInvocation {

	private static final String PATH = "/repository/fetch/data/";

	private String baseUrl;
	private String sessionId;
	private String repositoryId;
	private String commitId;
	private ModelType type;
	private String refId;

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public void setType(ModelType type) {
		this.type = type;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}

	/**
	 * Retrieves a data set matching the specified type, refId for the given
	 * commit id.
	 * 
	 * @return The requested openLCA entity as JsonObject
	 * @throws WebRequestException
	 *             If user is out of sync or has no access to the specified
	 *             repository
	 */
	public JsonObject execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		Valid.checkNotEmpty(commitId, "commit id");
		Valid.checkNotEmpty(type, "model type");
		Valid.checkNotEmpty(refId, "reference id");
		String url = Strings.concat(baseUrl, PATH, repositoryId, "/", type, "/", refId, "/", commitId);
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		String json = response.getEntity(String.class);
		JsonElement element = new Gson().fromJson(json, JsonElement.class);
		return element.getAsJsonObject();
	}

}
