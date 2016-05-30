package org.openlca.cloud.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.cloud.model.data.Dataset;
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

	private static final String PATH = "/sync/";
	String baseUrl;
	String sessionId;
	String repositoryId;
	String lastCommitId;
	String untilCommitId;
	List<Dataset> localChanges;
	
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
	List<FetchRequestData> execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (lastCommitId == null || lastCommitId.isEmpty())
			lastCommitId = "null";
		if (untilCommitId == null || untilCommitId.isEmpty())
			untilCommitId = "null";
		if (localChanges == null)
			localChanges = new ArrayList<>();
		String url = baseUrl + PATH + repositoryId + "/" + lastCommitId + "/" + untilCommitId;
		ClientResponse response = WebRequests.call(Type.POST, url, sessionId, localChanges);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return Collections.emptyList();
		return new Gson().fromJson(response.getEntity(String.class),
				new TypeToken<List<FetchRequestData>>() {
				}.getType());
	}

}
