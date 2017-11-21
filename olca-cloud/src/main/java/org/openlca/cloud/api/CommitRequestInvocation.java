package org.openlca.cloud.api;

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

	String baseUrl;
	String sessionId;
	String repositoryId;
	String lastCommitId;

	/**
	 * Requests a sync check on the specified repository
	 * 
	 * @throws WebRequestException
	 *             if user is not in sync with the repository or has no access
	 *             rights to the specified repository. To check if the user is
	 *             in sync, the last commit id (that id of the last commit that
	 *             was fetched) is send along with the request. If it does not
	 *             match the last commit id in the repository, the user is out
	 *             of sync
	 */
	public void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(repositoryId, "repository id");
		String url = baseUrl + PATH + repositoryId;
		if (lastCommitId != null) {
			url += "?lastCommitId=" + lastCommitId;
		}
		WebRequests.call(Type.GET, url, sessionId);
	}

}
