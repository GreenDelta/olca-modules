package org.openlca.cloud.api;

import javax.ws.rs.core.Response.Status;

import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.core.model.ModelType;

import com.sun.jersey.api.client.ClientResponse;

/**
 * Invokes a web service call to retrieve the previous commit id of a specified
 * reference
 */
class PreviousCommitInvocation {

	private static final String PATH = "/history/previousCommitId/";
	String baseUrl;
	String sessionId;
	String repositoryId;
	ModelType type;
	String refId;
	String commitId;

	/**
	 * Retrieves the id of the previous commit for this reference
	 * 
	 * @throws WebRequestException
	 *             If the user has no access to the specified repository
	 */
	String execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(repositoryId, "repository id");
		Valid.checkNotEmpty(type, "model type");
		Valid.checkNotEmpty(refId, "ref id");
		Valid.checkNotEmpty(commitId, "commit id");
		String url = baseUrl + PATH + repositoryId + "/" + type.name() + "/" + refId + "/" + commitId;
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		if (response.getStatus() == Status.NOT_FOUND.getStatusCode())
			return null;
		return response.getEntity(String.class);
	}
}
