package org.openlca.cloud.api;

import org.openlca.cloud.model.data.DatasetEntry;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.core.model.ModelType;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.greendelta.search.wrapper.SearchResult;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Invokes a web service call to search for data sets
 */
class SearchInvocation {

	private static final String PATH = "/public/search";
	String baseUrl;
	String sessionId;
	String query;
	int page = 1;
	int pageSize = 10;
	ModelType type;
	String repositoryId;

	/**
	 * Retrieves a search result
	 * 
	 * @return The search result
	 */
	SearchResult<DatasetEntry> execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		String url = baseUrl + PATH + "?page=" + page + "&pageSize=" + pageSize;
		if (!Strings.isNullOrEmpty(query)) {
			url += "&query=" + query;
		}
		if (type != null) {
			url += "&type=" + type.name();
		}
		if (!Strings.isNullOrEmpty(repositoryId)) {
			url += "&repositoryId=" + repositoryId;
		}
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		return new Gson().fromJson(response.getEntity(String.class),
				new TypeToken<SearchResult<DatasetEntry>>() {
				}.getType());
	}

}
