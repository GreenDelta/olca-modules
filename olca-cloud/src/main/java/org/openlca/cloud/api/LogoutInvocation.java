package org.openlca.cloud.api;

import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

/**
 * Invokes a web service call to logout
 */
class LogoutInvocation {

	private final static String PATH = "/public/logout";
	String baseUrl;
	String sessionId;

	/**
	 * Terminate the current user session
	 * 
	 * @throws WebRequestException
	 *             if the user was not logged in
	 */
	void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		String url = baseUrl + PATH;
		WebRequests.call(Type.POST, url, sessionId);
	}
}
