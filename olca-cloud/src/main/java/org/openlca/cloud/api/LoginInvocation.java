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
	String username;
	String password;

	/**
	 * Login with the specificied credentials
	 * 
	 * @throws WebRequestException
	 *             if the credentials were invalid or the user is already logged
	 *             in
	 */
	String execute() throws WebRequestException, TokenRequiredException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(username, "username");
		Valid.checkNotEmpty(password, "password");
		String url = baseUrl + PATH;
		Map<String, String> data = new HashMap<>();
		data.put("username", username);
		data.put("password", password);
		ClientResponse response = WebRequests.call(Type.POST, url, null, data);
		if (response.getStatus() != Status.OK.getStatusCode())
			return null;
		String result = response.getEntity(String.class);
		if ("tokenRequired".equals(result))
			throw new TokenRequiredException();
		for (NewCookie cookie : response.getCookies())
			if (cookie.getName().equals("JSESSIONID"))
				return cookie.getValue();
		return null;
	}
	
	public class TokenRequiredException extends Exception {

		private static final long serialVersionUID = -3172312730216177292L;
		
		
	}
	
}
