package com.greendelta.cloud.api;

import java.util.HashMap;
import java.util.Map;

import com.greendelta.cloud.util.Strings;
import com.greendelta.cloud.util.Valid;
import com.greendelta.cloud.util.WebRequests;
import com.greendelta.cloud.util.WebRequests.Type;
import com.greendelta.cloud.util.WebRequests.WebRequestException;

/**
 * Invokes a web service call to create a new user (admin operation)
 */
public class CreateUserInvocation {

	private static final String PATH = "/user/create";
	private String baseUrl;
	private String username;
	private String password;
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

	public void setPassword(String password) {
		this.password = password;
	}

	public void setAdminKey(String adminKey) {
		this.adminKey = adminKey;
	}

	/**
	 * Creates a new user with the specified username and password
	 * 
	 * @throws WebRequestException
	 *             if a user with the specified name already exists
	 */
	public void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(username, "username");
		Valid.checkNotEmpty(password, "password");
		Valid.checkNotEmpty(adminKey, "adminKey");
		String url = Strings.concat(baseUrl, PATH);
		Map<String, String> data = new HashMap<>();
		data.put("username", username);
		data.put("password", password);
		data.put("adminKey", adminKey);
		WebRequests.call(Type.POST, url, sessionId, data);
	}

}
