package org.openlca.cloud.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openlca.cloud.util.Strings;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Invokes a web service call to check if the given ref ids are contained in any
 * known library (e.g. openLCA reference data)
 */
class LibraryCheckInvocation {

	private static final String PATH = "/library/";
	String baseUrl;
	String sessionId;
	List<String> refIds;

	/**
	 * Retrieves the libraries for the given ref ids
	 * 
	 * @return A mapping from ref id to library name for those ref ids that are
	 *         contained in a library
	 * @throws WebRequestException
	 */
	Map<String, String> execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(refIds, "ref ids");
		String url = Strings.concat(baseUrl, PATH);
		ClientResponse response = WebRequests.call(Type.POST, url, sessionId,
				refIds);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return Collections.emptyMap();
		return new Gson().fromJson(response.getEntity(String.class),
				new TypeToken<Map<String, String>>() {
				}.getType());
	}

}
