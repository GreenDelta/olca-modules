package org.openlca.cloud.api;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.NewCookie;

import org.openlca.cloud.util.Strings;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Invokes a web service call to login
 */
class LoginInvocation {

	private static final String PATH = "/public/login";
	private String baseUrl;
	private String username;
	private String password;

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Login with the specificied credentials
	 * 
	 * @throws WebRequestException
	 *             if the credentials were invalid or the user is already logged in
	 */
	public String execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(username, "username");
		Valid.checkNotEmpty(password, "password");
		String url = Strings.concat(baseUrl, PATH);
		Map<String, String> data = new HashMap<>();
		data.put("username", username);
		data.put("password", password);
		ClientResponse response = WebRequests.call(Type.POST, url, null, data);
		for (NewCookie cookie : response.getCookies())
			if (cookie.getName().equals("JSESSIONID"))
				return cookie.getValue();
		return null;
	}

}
