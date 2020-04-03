package org.openlca.cloud.api;

import java.io.InputStream;
import java.util.function.Consumer;

import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Invokes a webservice call to export a repository
 */
class ExportRepositoryInvocation {

	private static final String PATH = "/repository/export/";
	String baseUrl;
	String sessionId;
	String repositoryId;
	Consumer<InputStream> onStream;

	InputStream execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(repositoryId, "repository id");
		ClientResponse response = WebRequests.call(Type.GET, baseUrl + PATH + repositoryId, sessionId);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return null;
		return response.getEntityInputStream();
	}

}
