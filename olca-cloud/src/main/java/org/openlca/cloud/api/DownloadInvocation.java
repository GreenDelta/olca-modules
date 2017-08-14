package org.openlca.cloud.api;

import java.util.HashSet;
import java.util.Set;

import org.openlca.cloud.model.data.FileReference;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.core.database.IDatabase;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.config.ClientConfig;

/**
 * Invokes a web service call to request a list of data sets to import into
 * openLCA
 */
class DownloadInvocation {

	private static final String PATH = "/public/sync/get/";
	private final IDatabase database;
	private final FetchNotifier notifier;
	ClientConfig config;
	String baseUrl;
	String sessionId;
	String repositoryId;
	String untilCommitId;
	Set<FileReference> requestData;

	DownloadInvocation(IDatabase database, FetchNotifier notifier) {
		this.database = database;
		this.notifier = notifier;
	}

	/**
	 * Retrieves the requested data sets
	 * 
	 * @throws WebRequestException
	 *             If user has no access to the specified repository
	 */
	void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (database == null)
			return;
		if (untilCommitId == null || untilCommitId.isEmpty())
			untilCommitId = "null";
		if (requestData == null)
			requestData = new HashSet<>();
		String url = baseUrl + PATH + repositoryId + "/" + untilCommitId;
		ClientResponse response = WebRequests.call(Type.PUT, url, sessionId, requestData, config);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return;
		new FetchHandler(database, notifier).handleResponse(response.getEntityInputStream());
	}

}
