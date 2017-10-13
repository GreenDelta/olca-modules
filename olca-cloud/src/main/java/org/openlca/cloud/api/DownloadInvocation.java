package org.openlca.cloud.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.cloud.model.data.FileReference;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Invokes a web service call to request a list of data sets to import into
 * openLCA
 */
class DownloadInvocation {

	private static final String PATH = "/public/sync/get/";
	private final IDatabase database;
	private final FetchNotifier notifier;
	String baseUrl;
	String sessionId;
	String repositoryId;
	String untilCommitId;
	Set<FileReference> requestData;
	boolean clearDatabase;

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
		ClientResponse response = WebRequests.call(Type.PUT, url, sessionId, requestData);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return;
		if (clearDatabase) {
			clearDatabase();
		}
		new FetchHandler(database, notifier).handleResponse(response.getEntityInputStream());
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
