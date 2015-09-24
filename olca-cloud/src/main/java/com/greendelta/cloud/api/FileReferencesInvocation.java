package com.greendelta.cloud.api;

import java.util.List;

import com.greendelta.cloud.model.data.FileReference;
import com.greendelta.cloud.util.Strings;
import com.greendelta.cloud.util.Valid;
import com.greendelta.cloud.util.WebRequests;
import com.greendelta.cloud.util.WebRequests.Type;
import com.greendelta.cloud.util.WebRequests.WebRequestException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

/**
 * Invokes a web service call to retrieve all file references contained in the
 * specified commit
 */
class FileReferencesInvocation {

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
	 * Retrieves all file references that have been committed in the specified
	 * commit
	 * 
	 * @return All file references of the specified commit, as list of file
	 *         references
	 * @throws WebRequestException
	 *             If the commit was not found for the given id or user has no
	 *             access to the specified repository
	 */
	public List<FileReference> execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		Valid.checkNotEmpty(commitId, "commit id");
		String url = Strings.concat(baseUrl, PATH, repositoryId, "/", commitId);
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		return response.getEntity(new GenericType<List<FileReference>>() {
		});
	}

}
