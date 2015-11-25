package org.openlca.cloud.api;

import org.openlca.cloud.util.Strings;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

/**
 * Invokes a web service call to check if the user is in sync with the specified
 * repository. In order to reduce the amount of data sent, this method should be
 * used to check if the user’s local db is in sync with the remote repository
 * before committing data
 */
class CommitRequestInvocation {

	private static final String PATH = "/commit/request/";

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
	 * Requests a sync check on the specified repository
	 * 
	 * @throws WebRequestException
	 *             if user is not in sync with the repository or has no access
	 *             rights to the specified repository. To check if the user is
	 *             in sync, the last commit id (that id of the last commit
	 *             that was fetched) is send along with the request. If it does
	 *             not match the last commit id in the repository, the user is
	 *             out of sync
	 */
	public void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (lastCommitId == null || lastCommitId.isEmpty())
			lastCommitId = "null";
		String url = Strings.concat(baseUrl, PATH, repositoryId, "/", lastCommitId);
		WebRequests.call(Type.GET, url, sessionId);
	}

}
