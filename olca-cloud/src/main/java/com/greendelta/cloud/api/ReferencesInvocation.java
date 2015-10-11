package com.greendelta.cloud.api;

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

/**
 * Invokes a web service call to retrieve all references contained in the
 * specified commit
 */
class ReferencesInvocation {

	private static final String PATH = "/repository/fetch/references/";

	private String baseUrl;
	private String sessionId;
	private String repositoryId;
	private String commitId;

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

	/**
	 * Retrieves all references that have been committed in the specified
	 * commit
	 * 
	 * @return All references of the specified commit, as list of file
	 *         references
	 * @throws WebRequestException
	 *             If the commit was not found for the given id or user has no
	 *             access to the specified repository
	 */
	public List<FetchRequestData> execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		Valid.checkNotEmpty(commitId, "commit id");
		String url = Strings.concat(baseUrl, PATH, repositoryId, "/", commitId);
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		return new Gson().fromJson(response.getEntity(String.class),
				new TypeToken<List<FetchRequestData>>() {
				}.getType());
	}

}
