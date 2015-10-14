package com.greendelta.cloud.api;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.greendelta.cloud.util.Strings;
import com.greendelta.cloud.util.WebRequests;
import com.greendelta.cloud.util.WebRequests.Type;
import com.greendelta.cloud.util.WebRequests.WebRequestException;
import com.sun.jersey.api.client.ClientResponse;

class UserAccessListInvocation {

	private static final String PATH = "/user/shared";
	private String baseUrl;
	private String sessionId;

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * Loads the list of repositories that the specified user has access to
	 * 
	 * @return list of accessible repositories
	 * @throws WebRequestException
	 */
	public List<String> execute() throws WebRequestException {
		String url = Strings.concat(baseUrl, PATH);
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		return new Gson().fromJson(response.getEntity(String.class),
				new TypeToken<List<String>>() {
				}.getType());
	}

}
