package org.openlca.cloud.api;

import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

/**
 * Invokes a webservice call to check access to the specified repository
 */
class CheckAccessInvocation {

	private static final String PATH = "/repository";
	String baseUrl;
	String sessionId;
	String repositoryId;

	/**
	 * Checks if the specified repository can be access by the current user
	 * 
	 * @throws WebRequestException
	 *             if repository does not exist or user does not have access
	 */
	void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		String url = baseUrl + PATH + "/" + repositoryId;
		WebRequests.call(Type.GET, url, sessionId);
	}

}
