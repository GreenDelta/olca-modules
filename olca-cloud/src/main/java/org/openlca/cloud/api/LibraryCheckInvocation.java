package org.openlca.cloud.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.cloud.model.LibraryRestriction;
import org.openlca.cloud.model.RestrictionType;
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

	private static final String PATH = "/library";
	String baseUrl;
	String sessionId;
	String repositoryId;
	Set<Dataset> datasets;

	/**
	 * Retrieves the libraries for the given ref ids
	 * 
	 * @return A mapping from ref id to library name for those ref ids that are
	 *         contained in a library
	 * @throws WebRequestException
	 */
	List<LibraryRestriction> execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(datasets, "datasets");
		String url = baseUrl + PATH + "?group=" + repositoryId.split("/")[0] + "&name=" + repositoryId.split("/")[1];
		List<String> refIds = new ArrayList<>();
		for (Dataset dataset : datasets)
			refIds.add(dataset.refId);
		ClientResponse response = WebRequests.call(Type.POST, url, sessionId, refIds);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return Collections.emptyList();
		return mapResults(response);
	}

	private List<LibraryRestriction> mapResults(ClientResponse response) {
		String entity = response.getEntity(String.class).trim();
		if (entity.startsWith("[")) {
			List<LibraryRestriction> list = new Gson().fromJson(entity, new TypeToken<List<LibraryRestriction>>() {
			}.getType());
			addDatasetToResults(list);
			return list;
		}
		Map<String, String> map = new Gson().fromJson(entity, new TypeToken<Map<String, Object>>() {
		}.getType());
		return legacyMapResults(map);
	}

	private void addDatasetToResults(List<LibraryRestriction> result) {
		Map<String, Dataset> map = new HashMap<>();
		for (Dataset dataset : datasets)
			map.put(dataset.refId, dataset);
		for (LibraryRestriction restriction : result) {
			restriction.dataset = map.get(restriction.datasetRefId);
		}
	}

	// support for collaboration server <= 1.1.2
	private List<LibraryRestriction> legacyMapResults(Map<String, String> result) {
		Map<String, Dataset> map = new HashMap<>();
		for (Dataset dataset : datasets)
			map.put(dataset.refId, dataset);
		List<LibraryRestriction> restrictions = new ArrayList<>();
		for (String refId : result.keySet()) {
			LibraryRestriction restriction = new LibraryRestriction(refId, result.get(refId), RestrictionType.WARNING);
			restriction.dataset = map.get(refId);
			restrictions.add(restriction);
		}
		return restrictions;
	}

}
