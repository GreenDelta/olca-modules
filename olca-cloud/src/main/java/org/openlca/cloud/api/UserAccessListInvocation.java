package org.openlca.cloud.api;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.openlca.cloud.util.Strings;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

import com.sun.jersey.api.client.ClientResponse;

class UserAccessListInvocation {

	private static final String PATH = "/access/shared/user";
	String baseUrl;
	String sessionId;

	/**
	 * Loads the list of repositories that the specified user has access to
	 * 
	 * @return list of accessible repositories
	 * @throws WebRequestException
	 */
	List<String> execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		String url = Strings.concat(baseUrl, PATH);
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		return new Gson().fromJson(response.getEntity(String.class),
				new TypeToken<List<String>>() {
				}.getType());
	}

}
