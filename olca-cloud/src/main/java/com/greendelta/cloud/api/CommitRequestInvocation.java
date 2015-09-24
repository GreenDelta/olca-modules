package com.greendelta.cloud.api;

import com.greendelta.cloud.util.Strings;
import com.greendelta.cloud.util.Valid;
import com.greendelta.cloud.util.WebRequests;
import com.greendelta.cloud.util.WebRequests.Type;
import com.greendelta.cloud.util.WebRequests.WebRequestException;

/**
 * Invokes a web service call to check if the user is in sync with the specified
 * repository. In order to reduce the amount of data sent, this method should be
 * used to check if the user’s local db is in sync with the remote repository
 * before committing data
 */
class CommitRequestInvocation {

	private static final String PATH = "/repository/commit/request/";

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
	 * Requests a sync check on the specified repository
	 * 
	 * @throws WebRequestException
	 *             if user is not in sync with the repository or has no access
	 *             rights to the specified repository. To check if the user is
	 *             in sync, the latest commit id (that id of the last commit
	 *             that was fetched) is send along with the request. If it does
	 *             not match the latest commit id in the repository, the user is
	 *             out of sync
	 */
	public void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (latestCommitId == null || latestCommitId.isEmpty())
			latestCommitId = "null";
		String url = Strings.concat(baseUrl, PATH, repositoryId, "/", latestCommitId);
		WebRequests.call(Type.GET, url, sessionId);
	}

}
