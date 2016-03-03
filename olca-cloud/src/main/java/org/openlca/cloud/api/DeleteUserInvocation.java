package org.openlca.cloud.api;

import java.util.HashMap;
import java.util.Map;

import org.openlca.cloud.util.Strings;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

/**
 * Invokes a web service call to delete a user
 */
class DeleteUserInvocation {

	private static final String PATH = "/user/delete";
	String baseUrl;
	String sessionId;
	String username;
	String adminKey;

	/**
	 * Deletes the user with the specified username
	 * 
	 * @throws WebRequestException
	 *             if a user with the specified name did not exists
	 */
	void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(username, "username");
		Valid.checkNotEmpty(adminKey, "adminKey");
		String url = Strings.concat(baseUrl, PATH, "/", username);
		Map<String, String> data = new HashMap<>();
		data.put("adminKey", adminKey);
		WebRequests.call(Type.DELETE, url, sessionId, data);
	}

}
