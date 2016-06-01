package org.openlca.cloud.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.openlca.cloud.api.data.FetchReader;
import org.openlca.cloud.model.data.Dataset;
import org.openlca.cloud.util.Directories;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.core.database.CategorizedEntityDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Invokes a web service call to request a list of data sets to import into
 * openLCA
 */
class DownloadInvocation {

	private static final Logger log = LoggerFactory.getLogger(DownloadInvocation.class);
	private static final String PATH = "/sync/get/";
	private final IDatabase database;
	String baseUrl;
	String sessionId;
	String repositoryId;
	String untilCommitId;
	List<Dataset> requestData;

	DownloadInvocation(IDatabase database) {
		this.database = database;
	}

	/**
	 * Retrieves the requested data sets
	 * 
	 * @throws WebRequestException
	 *             If user has no access to the specified repository
	 */
	void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (database == null)
			return;
		if (untilCommitId == null || untilCommitId.isEmpty())
			untilCommitId = "null";
		if (requestData == null || requestData.isEmpty())
			return;
		String url = baseUrl + PATH + repositoryId + "/" + untilCommitId;
		ClientResponse response = WebRequests.call(Type.PUT, url, sessionId, requestData);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return;
		handleResponse(response.getEntityInputStream());
	}

	private void handleResponse(InputStream data) {
		Path dir = null;
		FetchReader reader = null;
		try {
			dir = Files.createTempDirectory("fetchReader");
			File zipFile = new File(dir.toFile(), "fetch.zip");
			Files.copy(data, zipFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			reader = new FetchReader(zipFile);
			doImport(reader);
		} catch (IOException e) {
			log.error("Error reading download data", e);
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

	private void doImport(FetchReader reader) {
		JsonImport jsonImport = new JsonImport(reader.getEntityStore(), database);
		jsonImport.setUpdateMode(UpdateMode.ALWAYS);
		jsonImport.run();
		for (Dataset descriptor : reader.getDescriptors())
			if (!reader.hasData(descriptor))
				delete(Daos.createCategorizedDao(database, descriptor.type), descriptor.refId);
	}

	private <T extends CategorizedEntity, V extends CategorizedDescriptor> void delete(
			CategorizedEntityDao<T, V> dao, String refId) {
		dao.delete(dao.getForRefId(refId));
	}

}
