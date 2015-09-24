package com.greendelta.cloud.api;

import com.greendelta.cloud.util.Strings;
import com.greendelta.cloud.util.Valid;
import com.greendelta.cloud.util.WebRequests;
import com.greendelta.cloud.util.WebRequests.Type;
import com.greendelta.cloud.util.WebRequests.WebRequestException;

/**
 * Invokes a web service call to create a new repository
 */
class CreateRepositoryInvocation {

	private static final String PATH = "/repository/create";

	private String baseUrl;
	private String sessionId;
	private String name;

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Creates a new repository with the specified name
	 * 
	 * @throws WebRequestException
	 *             if a repository with the specified name already exists
	 */
	public void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(name, "repository name");
		String url = Strings.concat(baseUrl, PATH, "/", name);
		WebRequests.call(Type.POST, url, sessionId);
	}

}
