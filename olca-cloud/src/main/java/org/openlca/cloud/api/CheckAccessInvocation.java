package org.openlca.cloud.api;

import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.jsonld.Schema;
import org.openlca.jsonld.Schema.UnsupportedSchemaException;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Invokes a webservice call to check access to the specified repository
 */
class CheckAccessInvocation {

	private static final String PATH = "/repository/meta";
	String baseUrl;
	String sessionId;
	String repositoryId;

	/**
	 * Checks if the specified repository can be access by the current user and
	 * fits the schema version
	 * 
	 * @throws WebRequestException
	 *             if repository does not exist or user does not have access
	 */
	void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		String url = baseUrl + PATH + "/" + repositoryId;
		try {
			ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
			String json = response.getEntity(String.class);
			MetaInfo meta = new Gson().fromJson(json, MetaInfo.class);
			if (!Schema.isSupportedSchema(meta.schemaVersion))
				throw new UnsupportedSchemaException(meta.schemaVersion);
		} catch (WebRequestException e) {
			if (!Strings.isNullOrEmpty(e.getMessage()))
				throw e; // repository does not exist or no access
			// url does not exist -> old server
			throw new UnsupportedSchemaException("");
		}
	}

	private static class MetaInfo {

		private String schemaVersion;

	}

}
