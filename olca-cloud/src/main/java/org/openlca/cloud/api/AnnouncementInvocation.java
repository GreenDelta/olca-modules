package org.openlca.cloud.api;

import org.openlca.cloud.model.Announcement;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Invokes a webservice call to check for server announcements
 */
class AnnouncementInvocation {

	private static final String PATH = "/public/announcements";
	String baseUrl;
	String sessionId;

	Announcement execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		ClientResponse response = WebRequests.call(Type.GET, baseUrl + PATH, sessionId);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return null;
		return new Gson().fromJson(response.getEntity(String.class), Announcement.class);
	}

}
