package org.openlca.cloud.api;

import org.openlca.cloud.util.Strings;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

/**
 * Invokes a webservice call to cancel sharing the specified repository
 */
class UnshareRepositoryInvocation {

	private static final String PATH = "/repository/unshare";
	private String baseUrl;
	private String sessionId;
	private String repositoryName;
	private String unshareWithUser;

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public void setUnshareWithUser(String unshareWithUser) {
		this.unshareWithUser = unshareWithUser;
	}

	/**
	 * Cancels sharing of the specified repository with the specified user
	 * 
	 * @throws WebRequestException
	 *             if repository or user does not exist
	 */
	public void execute() throws WebRequestException {
		String url = Strings.concat(baseUrl, PATH, "/", repositoryName, "/", unshareWithUser);
		WebRequests.call(Type.POST, url, sessionId);
	}

}
