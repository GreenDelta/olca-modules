package org.openlca.cloud.api;

import org.openlca.core.model.ModelType;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.cloud.util.Strings;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Invokes a web service call to load a data set matching by type, refId and
 * commitId
 */
class DatasetContentInvocation {

	private static final String PATH = "/fetch/data/";
	String baseUrl;
	String sessionId;
	String repositoryId;
	String commitId;
	ModelType type;
	String refId;

	/**
	 * Retrieves a data set matching the specified type, refId for the given
	 * commit id.
	 * 
	 * @return The requested openLCA entity as JsonObject
	 * @throws WebRequestException
	 *             If user is out of sync or has no access to the specified
	 *             repository
	 */
	JsonObject execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		Valid.checkNotEmpty(type, "model type");
		Valid.checkNotEmpty(refId, "reference id");
		if (commitId == null)
			commitId = "null";
		String url = Strings.concat(baseUrl, PATH, repositoryId, "/", type, "/", refId, "/", commitId);
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		String json = response.getEntity(String.class);
		JsonElement element = new Gson().fromJson(json, JsonElement.class);
		return element.getAsJsonObject();
	}

}
