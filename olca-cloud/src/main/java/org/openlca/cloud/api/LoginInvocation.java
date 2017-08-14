package org.openlca.cloud.api;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.NewCookie;

import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Invokes a web service call to login
 */
class LoginInvocation {

	private static final String PATH = "/public/login";
	String baseUrl;
	CredentialSupplier credentials;

	/**
	 * Login with the specificied credentials
	 * 
	 * @throws WebRequestException
	 *             if the credentials were invalid or the user is already logged
	 *             in
	 */
	String execute() throws WebRequestException {
		ClientResponse response = _execute(null);
		if (response.getStatus() != Status.OK.getStatusCode())
			return null;
		String result = response.getEntity(String.class);
		if ("tokenRequired".equals(result)) {
			Integer token = credentials.getToken();
			if (token == null)
				return null;
			response = _execute(token);
		}
		for (NewCookie cookie : response.getCookies())
			if (cookie.getName().equals("JSESSIONID"))
				return cookie.getValue();
		return null;
	}

	private ClientResponse _execute(Integer token) throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(credentials.username, "username");
		Valid.checkNotEmpty(credentials.password, "password");
		String url = baseUrl + PATH;
		Map<String, String> data = new HashMap<>();
		data.put("username", credentials.username);
		data.put("password", credentials.password);
		if (token != null)
			data.put("token", token.toString());
		return WebRequests.call(Type.POST, url, null, data);
	}

}
