package org.openlca.cloud.api;

import java.util.Collections;
import java.util.Set;

import org.openlca.cloud.model.data.FetchRequestData;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.config.ClientConfig;

/**
 * Invokes a web service call to request a list of data set descriptors of those
 * data sets that have been changed after the specified commit id
 */
class FetchRequestInvocation {

	private static final String PATH = "/fetch/request/";
	ClientConfig config;
	String baseUrl;
	String sessionId;
	String repositoryId;
	String lastCommitId;

	/**
	 * Retrieves all changed data sets (only the descriptors)
	 * 
	 * @return A list of data set descriptors for those data sets that have been
	 *         changed since the last fetch
	 * @throws WebRequestException
	 *             If user has no access to the specified repository
	 */
	Set<FetchRequestData> execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (lastCommitId == null || lastCommitId.isEmpty())
			lastCommitId = "null";
		String url = baseUrl + PATH + repositoryId + "/" + lastCommitId;
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId, config);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return Collections.emptySet();
		return new Gson().fromJson(response.getEntity(String.class),
				new TypeToken<Set<FetchRequestData>>() {
				}.getType());
	}

}
