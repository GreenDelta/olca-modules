package org.openlca.cloud.api;

import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.openlca.cloud.model.data.Commit;
import org.openlca.cloud.util.Strings;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Invokes a web service call to retrieve all commit entries after the specified
 * commit id
 */
class HistoryInvocation {

	private static final String PATH = "/history/";

	private String baseUrl;
	private String sessionId;
	private String repositoryId;
	private String lastCommitId;

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public void setLastCommitId(String lastCommitId) {
		this.lastCommitId = lastCommitId;
	}

	/**
	 * Retrieves all commit entries that have been committed after the last
	 * fetch, specified by the lastCommitId
	 * 
	 * @return All commit entries since the last fetch, as list of commit
	 *         descriptors
	 * @throws WebRequestException
	 *             If user has no access to the specified repository
	 */
	public List<Commit> execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (lastCommitId == null || lastCommitId.isEmpty())
			lastCommitId = "null";
		String url = Strings.concat(baseUrl, PATH, repositoryId, "/",
				lastCommitId);
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return Collections.emptyList();
		return new Gson().fromJson(response.getEntity(String.class),
				new TypeToken<List<Commit>>() {
				}.getType());
	}

}
