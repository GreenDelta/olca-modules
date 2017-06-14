package org.openlca.cloud.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

class CheckoutInvocation {

	private static final String PATH = "/public/sync/get/";
	private final IDatabase database;
	private final FetchNotifier notifier;
	String baseUrl;
	String sessionId;
	String repositoryId;
	String commitId;

	CheckoutInvocation(IDatabase database, FetchNotifier notifier) {
		this.database = database;
		this.notifier = notifier;
	}
	/**
	 * Retrieves all data sets until the specified commit
	 * 
	 * @throws WebRequestException
	 *             If user is out of sync or has no access to the specified
	 *             repository
	 */
	void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (commitId == null || commitId.isEmpty())
			commitId = "null";
		String url = baseUrl + PATH + repositoryId + "/" + commitId;
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return;
		clearDatabase();
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
