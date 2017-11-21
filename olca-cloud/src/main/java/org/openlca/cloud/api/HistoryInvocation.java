package org.openlca.cloud.api;

import java.util.Collections;
import java.util.List;

import org.openlca.cloud.model.data.Commit;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Invokes a web service call to retrieve all commit entries after the specified
 * commit id
 */
class HistoryInvocation {

	private static final String PATH = "/history/";
	String baseUrl;
	String sessionId;
	String repositoryId;
	String lastCommitId;

	/**
	 * Retrieves all commit entries that have been committed after the last
	 * fetch, specified by the lastCommitId
	 * 
	 * @return All commit entries since the last fetch, as list of commit
	 *         descriptors
	 * @throws WebRequestException
	 *             If user has no access to the specified repository
	 */
	List<Commit> execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(repositoryId, "repository id");
		String url = baseUrl + PATH + repositoryId;
		if (lastCommitId != null) {
			url += "?lastCommitId=" + lastCommitId;
		}
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return Collections.emptyList();
		return new Gson().fromJson(response.getEntity(String.class),
				new TypeToken<List<Commit>>() {
				}.getType());
	}

}
