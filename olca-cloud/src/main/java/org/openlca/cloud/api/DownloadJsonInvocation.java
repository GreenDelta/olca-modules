package org.openlca.cloud.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

import org.openlca.cloud.model.data.FileReference;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

class DownloadJsonInvocation {

	private static final Logger log = LoggerFactory.getLogger(DownloadJsonInvocation.class);
	private static final String PATH = "/public/download/json/";
	String baseUrl;
	String sessionId;
	String repositoryId;
	Set<FileReference> requestData;

	File execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (requestData == null)
			requestData = new HashSet<>();
		String url = baseUrl + PATH + "prepare/" + repositoryId;
		ClientResponse response = WebRequests.call(Type.PUT, url, sessionId, requestData);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return null;
		String dlToken = response.getEntity(String.class);
		url = baseUrl + PATH + dlToken;
		response = WebRequests.call(Type.GET, url, sessionId);
		try {
			File tmp = Files.createTempFile("olca", ".zip").toFile();
			Files.copy(response.getEntityInputStream(), tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return tmp;
		} catch (IOException e) {
			log.error("Error importing data", e);
			return null;
		}
	}


}
