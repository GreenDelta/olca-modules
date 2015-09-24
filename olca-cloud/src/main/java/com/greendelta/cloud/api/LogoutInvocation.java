package com.greendelta.cloud.api;

import com.greendelta.cloud.util.Strings;
import com.greendelta.cloud.util.Valid;
import com.greendelta.cloud.util.WebRequests;
import com.greendelta.cloud.util.WebRequests.Type;
import com.greendelta.cloud.util.WebRequests.WebRequestException;

/**
 * Invokes a web service call to logout
 */
class LogoutInvocation {

	private final static String PATH = "/public/logout";

	private String baseUrl;
	private String sessionId;

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * Terminate the current user session
	 * 
	 * @throws WebRequestException
	 *             if the user was not logged in
	 */
	public void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		String url = Strings.concat(baseUrl, PATH);
		WebRequests.call(Type.POST, url, sessionId);
	}
}
