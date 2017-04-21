package org.openlca.cloud.api;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openlca.cloud.model.data.Dataset;
import org.openlca.cloud.model.data.FileReference;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.core.database.IDatabase;

import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Invokes a web service call to fetch the latest changes after the specified
 * commit id
 */
class FetchInvocation {

	private static final String PATH = "/fetch/";
	private final IDatabase database;
	private final FetchNotifier notifier;
	String baseUrl;
	String sessionId;
	String repositoryId;
	String lastCommitId;
	Set<FileReference> fetchData;
	Map<Dataset, JsonObject> mergedData;

	FetchInvocation(IDatabase database, FetchNotifier notifier) {
		this.database = database;
		this.notifier = notifier;
	}

	/**
	 * Retrieves all changed data sets since the last fetch
	 * 
	 * @return The latest commit id
	 * @throws WebRequestException
	 *             If user is out of sync or has no access to the specified
	 *             repository
	 */
	String execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (lastCommitId == null || lastCommitId.isEmpty())
			lastCommitId = "null";
		if (fetchData == null) // still call service to receive latest commit id
			fetchData = new HashSet<>();
		String url = baseUrl + PATH + repositoryId + "/" + lastCommitId;
		ClientResponse response = WebRequests.call(Type.POST, url, sessionId, fetchData);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return null;
		return new FetchHandler(database, mergedData, notifier).handleResponse(response.getEntityInputStream());
	}

}
