package com.greendelta.cloud.api;

import java.util.HashMap;
import java.util.Map;

import com.greendelta.cloud.util.Strings;
import com.greendelta.cloud.util.Valid;
import com.greendelta.cloud.util.WebRequests;
import com.greendelta.cloud.util.WebRequests.Type;
import com.greendelta.cloud.util.WebRequests.WebRequestException;

/**
 * Invokes a web service call to delete a user
 */
class DeleteUserInvocation {

	private static final String PATH = "/user/delete";
	private String baseUrl;
	private String username;
	private String adminKey;
	private String sessionId;

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setAdminKey(String adminKey) {
		this.adminKey = adminKey;
	}
	
	/**
	 * Deletes the user with the specified username
	 * 
	 * @throws WebRequestException
	 *             if a user with the specified name did not exists
	 */
	public void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(username, "username");
		Valid.checkNotEmpty(adminKey, "adminKey");
		String url = Strings.concat(baseUrl, PATH, "/", username);
		Map<String, String> data = new HashMap<>();
		data.put("adminKey", adminKey);
		WebRequests.call(Type.DELETE, url, sessionId, data);
	}

}
