package com.greendelta.cloud.api;

import com.greendelta.cloud.util.Strings;
import com.greendelta.cloud.util.WebRequests;
import com.greendelta.cloud.util.WebRequests.Type;
import com.greendelta.cloud.util.WebRequests.WebRequestException;

/**
 * Invokes a webservice call to share the specified repository
 */
class ShareRepositoryInvocation {

	private static final String PATH = "/repository/share";
	private String baseUrl;
	private String sessionId;
	private String repositoryName;
	private String shareWithUser;

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public void setShareWithUser(String shareWithUser) {
		this.shareWithUser = shareWithUser;
	}

	/**
	 * Shares the specified repository with the specified user
	 * 
	 * @throws WebRequestException
	 *             if repository or user does not exist
	 */
	public void execute() throws WebRequestException {
		String url = Strings.concat(baseUrl, PATH, "/", repositoryName, "/", shareWithUser);
		WebRequests.call(Type.POST, url, sessionId);
	}

}
