package org.openlca.cloud.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openlca.cloud.api.data.FetchReader;
import org.openlca.cloud.util.Directories;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

class CheckoutInvocation {

	private static final String PATH = "/checkout/";
	private final Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;
	String baseUrl;
	String sessionId;
	String repositoryId;
	String commitId;

	CheckoutInvocation(IDatabase database) {
		this.database = database;
	}

	/**
	 * Retrieves all data sets until the specified commit
	 * 
	 * @return The commit id
	 * @throws WebRequestException
	 *             If user is out of sync or has no access to the specified
	 *             repository
	 */
	void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (commitId == null || commitId.isEmpty())
			commitId = "null";
		String url = baseUrl + PATH + repositoryId + "/" + commitId;
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return;
		handleResponse(response.getEntityInputStream());
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

	private void handleResponse(InputStream data) {
		clearDatabase();
		Path dir = null;
		FetchReader reader = null;
		try {
			dir = Files.createTempDirectory("fetchReader");
			File zipFile = new File(dir.toFile(), "fetch.zip");
			Files.copy(data, zipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			reader = new FetchReader(zipFile);
			doImport(reader);
		} catch (IOException e) {
			log.error("Error reading fetch data", e);
		} finally {
			if (dir != null && dir.toFile().exists())
				Directories.delete(dir.toFile());
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					log.error("Error closing fetch reader", e);
				}
		}
	}

	private String doImport(FetchReader reader) {
		JsonImport jsonImport = new JsonImport(reader.getEntityStore(), database);
		jsonImport.setUpdateMode(UpdateMode.ALWAYS);
		jsonImport.run();
		return reader.getCommitId();
	}

}
