package org.openlca.cloud.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.cloud.model.data.Dataset;
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
	Set<Dataset> datasets;

	/**
	 * Retrieves the libraries for the given ref ids
	 * 
	 * @return A mapping from ref id to library name for those ref ids that are
	 *         contained in a library
	 * @throws WebRequestException
	 */
	Map<Dataset, String> execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(datasets, "datasets");
		String url = baseUrl + PATH;
		List<String> refIds = new ArrayList<>();
		for (Dataset dataset : datasets)
			refIds.add(dataset.refId);
		ClientResponse response = WebRequests.call(Type.POST, url, sessionId,
				refIds);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return Collections.emptyMap();
		return mapResults(response);
	}

	private Map<Dataset, String> mapResults(ClientResponse response) {
		Map<String, String> result = new Gson().fromJson(
				response.getEntity(String.class),
				new TypeToken<Map<String, String>>() {
				}.getType());
		Map<String, Dataset> map = new HashMap<>();
		for (Dataset dataset : datasets)
			map.put(dataset.refId, dataset);
		Map<Dataset, String> mapped = new HashMap<>();
		for (String refId : result.keySet())
			mapped.put(map.get(refId), result.get(refId));
		return mapped;
	}

}
