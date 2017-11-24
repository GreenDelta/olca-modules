package org.openlca.cloud.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.cloud.model.data.Dataset;
import org.openlca.cloud.model.data.FileReference;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

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
	String commitId;
	Set<FileReference> requestData;
	Map<Dataset, JsonObject> mergedData;
	boolean clearDatabase;
	// false=fetch (all after the specified commit id)
	// true=download (all until the specified commit id)
	boolean download;

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
		Valid.checkNotEmpty(repositoryId, "repository id");
		String url = baseUrl + PATH + repositoryId + "?download=" + download;
		if (commitId != null) {
			url += "&commitId=" + commitId;
		}
		if (requestData == null) {
			requestData = new HashSet<>();
		}
		ClientResponse response = WebRequests.call(Type.POST, url, sessionId, requestData);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return null;
		if (clearDatabase) {
			clearDatabase();
		}
		return new FetchHandler(database, mergedData, notifier).handleResponse(response.getEntityInputStream());
	}

	private void clearDatabase() {
		try {
			List<String> tables = new ArrayList<>();
			NativeSql.on(database).query("SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLETYPE = 'T'", (rs) -> {
				tables.add(rs.getString(1));
				return true;
			});
			for (String table : tables) {
				if (table.toUpperCase().equals("SEQUENCE"))
					continue;
				if (table.toUpperCase().equals("OPENLCA_VERSION"))
					continue;
				NativeSql.on(database).runUpdate("DELETE FROM " + table);
			}
			NativeSql.on(database).runUpdate("UPDATE SEQUENCE SET SEQ_COUNT = 0");
			database.getEntityFactory().getCache().evictAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
