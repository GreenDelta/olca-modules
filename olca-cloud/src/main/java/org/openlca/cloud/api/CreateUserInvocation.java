package org.openlca.cloud.api;

import java.util.HashMap;
import java.util.Map;

import org.openlca.cloud.util.Strings;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

/**
 * Invokes a web service call to create a new user (admin operation)
 */
class CreateUserInvocation {

	private static final String PATH = "/user/create";
	String baseUrl;
	String sessionId;
	String username;
	String password;
	String adminKey;

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
