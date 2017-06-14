package org.openlca.cloud.api;

import java.util.List;

import org.openlca.cloud.model.data.FetchRequestData;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Invokes a web service call to retrieve all references contained in the
 * specified commit
 */
class ReferencesInvocation {

	private static final String PATH = "/fetch/references/";
	String baseUrl;
	String sessionId;
	String repositoryId;
	String commitId;

	/**
	 * Retrieves all references that have been committed in the specified commit
	 * 
	 * @return All references of the specified commit, as list of file
	 *         references
	 * @throws WebRequestException
	 *             If the commit was not found for the given id or user has no
	 *             access to the specified repository
	 */
	List<FetchRequestData> execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(repositoryId, "repository id");
		Valid.checkNotEmpty(commitId, "commit id");
		String url = baseUrl + PATH + repositoryId + "/" + commitId;
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		return new Gson().fromJson(response.getEntity(String.class),
				new TypeToken<List<FetchRequestData>>() {
				}.getType());
	}

}
