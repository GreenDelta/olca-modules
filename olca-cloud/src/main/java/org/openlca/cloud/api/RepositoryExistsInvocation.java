package org.openlca.cloud.api;

import org.openlca.cloud.util.Strings;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

import com.sun.jersey.api.client.ClientResponse;

class RepositoryExistsInvocation {

	private static final String PATH = "/repository/exists";
	String baseUrl;
	String sessionId;
	String name;

	/**
	 * Checks if the specified repository exists for the given user
	 */
	boolean execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(name, "repository name");
		String url = Strings.concat(baseUrl, PATH, "/", name);
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		return Boolean.parseBoolean(response.getEntity(String.class));
	}

}
