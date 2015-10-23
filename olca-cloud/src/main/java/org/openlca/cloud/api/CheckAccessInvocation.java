package org.openlca.cloud.api;

import org.openlca.cloud.util.Strings;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

/**
 * Invokes a webservice call to check access to the specified repository
 */
class CheckAccessInvocation {

	private static final String PATH = "/user/access";
	private String baseUrl;
	private String sessionId;
	private String repositoryId;

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	/**
	 * Checks if the specified repository can be access by the specified user
	 * 
	 * @throws WebRequestException
	 *             if repository does not exist or user does not have access
	 */
	public void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		String url = Strings.concat(baseUrl, PATH, "/", repositoryId);
		WebRequests.call(Type.GET, url, sessionId);
	}

}
