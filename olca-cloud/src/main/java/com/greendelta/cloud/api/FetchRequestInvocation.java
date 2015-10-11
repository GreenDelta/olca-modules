package com.greendelta.cloud.api;

import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.greendelta.cloud.model.data.FetchRequestData;
import com.greendelta.cloud.util.Strings;
import com.greendelta.cloud.util.Valid;
import com.greendelta.cloud.util.WebRequests;
import com.greendelta.cloud.util.WebRequests.Type;
import com.greendelta.cloud.util.WebRequests.WebRequestException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Invokes a web service call to request a list of data set identifiers of those
 * data sets that have been changed after the specified commit id
 */
class FetchRequestInvocation {

	private static final String PATH = "/repository/fetch/request/";

	private String baseUrl;
	private String sessionId;
	private String repositoryId;
	private String latestCommitId;

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
	 * Retrieves all changed data sets (only the identifiers)
	 * 
	 * @return A list of data set identifiers for those data sets that have been
	 *         changed since the last fetch
	 * @throws WebRequestException
	 *             If user has no access to the specified repository
	 */
	public List<FetchRequestData> execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (latestCommitId == null || latestCommitId.isEmpty())
			latestCommitId = "null";
		String url = Strings.concat(baseUrl, PATH, repositoryId, "/",
				latestCommitId);
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return Collections.emptyList();
		return new Gson().fromJson(response.getEntity(String.class),
				new TypeToken<List<FetchRequestData>>() {
				}.getType());
	}

}
