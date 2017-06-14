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

/**
 * Invokes a web service call to request a list of data set descriptors of those
 * data sets that have been changed between the specified commit ids and for
 * those local descriptors send across as well
 */
class SyncInvocation {

	private static final String PATH = "/public/sync/";
	String baseUrl;
	String sessionId;
	String repositoryId;
	String untilCommitId;

	/**
	 * Retrieves data sets descriptors of all changed data sets and for
	 * requested locally changed data sets
	 * 
	 * @return A list of data set descriptors for those data sets that have been
	 *         changed since the last fetch and for additionally requested data
	 *         sets
	 * @throws WebRequestException
	 *             If user has no access to the specified repository
	 */
	Set<FetchRequestData> execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (untilCommitId == null || untilCommitId.isEmpty())
			untilCommitId = "null";
		String url = baseUrl + PATH + repositoryId + "/" + untilCommitId;
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return Collections.emptySet();
		return new Gson().fromJson(response.getEntity(String.class), new TypeToken<Set<FetchRequestData>>() {
		}.getType());
	}

}
