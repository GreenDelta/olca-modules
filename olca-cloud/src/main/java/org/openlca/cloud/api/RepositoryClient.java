package org.openlca.cloud.api;

import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.openlca.cloud.model.data.Commit;
import org.openlca.cloud.model.data.Dataset;
import org.openlca.cloud.model.data.FetchRequestData;
import org.openlca.cloud.model.data.FileReference;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.core.model.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class RepositoryClient {

	private final static Logger log = LoggerFactory.getLogger(RepositoryClient.class);
	private final RepositoryConfig config;
	private final ClientConfig jerseyConfig;
	// Method to call if token is required, if no callback is specified a
	// TokenRequiredException will be thrown when a token is required
	private String sessionId;

	public RepositoryClient(RepositoryConfig config) {
		this.config = config;
		this.jerseyConfig = createJerseyConfig();
	}

	public RepositoryConfig getConfig() {
		return config;
	}

	private boolean login() throws WebRequestException {
		LoginInvocation invocation = new LoginInvocation();
		invocation.config = jerseyConfig;
		invocation.baseUrl = config.baseUrl;
		invocation.credentials = config.credentials;
		sessionId = invocation.execute();
		return sessionId != null;
	}

	public void logout() throws WebRequestException {
		if (sessionId == null)
			return;
		LogoutInvocation invocation = new LogoutInvocation();
		invocation.config = jerseyConfig;
		invocation.baseUrl = config.baseUrl;
		invocation.sessionId = sessionId;
		try {
			invocation.execute();
		} catch (WebRequestException e) {
			if (e.getErrorCode() != Status.UNAUTHORIZED.getStatusCode())
				if (e.getErrorCode() != Status.CONFLICT.getStatusCode())
					throw e;
		}
		sessionId = null;
	}

	public boolean hasAccess(String repositoryId) throws WebRequestException {
		Boolean result = executeLoggedIn(() -> {
			CheckAccessInvocation invocation = new CheckAccessInvocation();
			invocation.config = jerseyConfig;
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = repositoryId;
			try {
				invocation.execute();
				return true;
			} catch (WebRequestException e) {
				if (e.getErrorCode() == Status.FORBIDDEN.getStatusCode())
					return false;
				throw e;
			}
		});
		if (result == null)
			return false;
		return result;
	}

	public boolean requestCommit() throws WebRequestException {
		Boolean result = executeLoggedIn(() -> {
			CommitRequestInvocation invocation = new CommitRequestInvocation();
			invocation.config = jerseyConfig;
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.lastCommitId = config.getLastCommitId();
			try {
				invocation.execute();
			} catch (WebRequestException e) {
				if (e.getErrorCode() == Status.CONFLICT.getStatusCode())
					return false;
				throw e;
			}
			return true;
		});
		if (result == null)
			return false;
		return result;
	}

	public void commit(String message, Set<Dataset> data, Consumer<Dataset> callback) throws WebRequestException {
		executeLoggedIn(() -> {
			CommitInvocation invocation = new CommitInvocation(config.database);
			invocation.config = jerseyConfig;
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.lastCommitId = config.getLastCommitId();
			invocation.message = message;
			invocation.data = data;
			config.setLastCommitId(invocation.execute(callback));
		});
	}

	public List<Commit> fetchCommitHistory() throws WebRequestException {
		List<Commit> result = executeLoggedIn(() -> {
			HistoryInvocation invocation = new HistoryInvocation();
			invocation.config = jerseyConfig;
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			return invocation.execute();
		});
		if (result == null)
			return new ArrayList<>();
		return result;
	}

	public List<Commit> fetchNewCommitHistory() throws WebRequestException {
		List<Commit> result = executeLoggedIn(() -> {
			HistoryInvocation invocation = new HistoryInvocation();
			invocation.config = jerseyConfig;
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.lastCommitId = config.getLastCommitId();
			return invocation.execute();
		});
		if (result == null)
			return new ArrayList<>();
		return result;
	}

	public Map<Dataset, String> performLibraryCheck(Set<Dataset> datasets) throws WebRequestException {
		Map<Dataset, String> result = executeLoggedIn(() -> {
			LibraryCheckInvocation invocation = new LibraryCheckInvocation();
			invocation.config = jerseyConfig;
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.datasets = datasets;
			return invocation.execute();
		});
		if (result == null)
			return new HashMap<>();
		return result;
	}

	public List<FetchRequestData> getReferences(String commitId) throws WebRequestException {
		List<FetchRequestData> result = executeLoggedIn(() -> {
			ReferencesInvocation invocation = new ReferencesInvocation();
			invocation.config = jerseyConfig;
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.commitId = commitId;
			return invocation.execute();
		});
		if (result == null)
			return new ArrayList<>();
		return result;

	}

	public String getPreviousReference(ModelType type, String refId, String beforeCommitId) throws WebRequestException {
		return executeLoggedIn(() -> {
			PreviousCommitInvocation invocation = new PreviousCommitInvocation();
			invocation.config = jerseyConfig;
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.type = type;
			invocation.refId = refId;
			invocation.commitId = beforeCommitId;
			return invocation.execute();
		});
	}

	public Set<FetchRequestData> requestFetch() throws WebRequestException {
		Set<FetchRequestData> result = executeLoggedIn(() -> {
			FetchRequestInvocation invocation = new FetchRequestInvocation();
			invocation.config = jerseyConfig;
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.lastCommitId = config.getLastCommitId();
			return invocation.execute();
		});
		if (result == null)
			return new HashSet<>();
		return result;
	}

	public Set<FetchRequestData> list() throws WebRequestException {
		return sync(null);
	}

	public Set<FetchRequestData> sync(String untilCommitId) throws WebRequestException {
		Set<FetchRequestData> result = executeLoggedIn(() -> {
			SyncInvocation invocation = new SyncInvocation();
			invocation.config = jerseyConfig;
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.untilCommitId = untilCommitId;
			return invocation.execute();
		});
		if (result == null)
			return new HashSet<>();
		return result;
	}

	public File downloadJson(Set<FileReference> requestData) throws WebRequestException {
		return executeLoggedIn(() -> {
			DownloadJsonInvocation invocation = new DownloadJsonInvocation();
			invocation.config = jerseyConfig;
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.requestData = requestData;
			return invocation.execute();
		});
	}

	public void download(Set<FileReference> requestData, String commitId, FetchNotifier notifier)
			throws WebRequestException {
		executeLoggedIn(() -> {
			DownloadInvocation invocation = new DownloadInvocation(config.database, notifier);
			invocation.config = jerseyConfig;
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.untilCommitId = commitId;
			invocation.requestData = requestData;
			invocation.execute();
		});
	}

	public void fetch(Set<FileReference> fetchData, Map<Dataset, JsonObject> mergedData, FetchNotifier notifier)
			throws WebRequestException {
		executeLoggedIn(() -> {
			FetchInvocation invocation = new FetchInvocation(config.database, notifier);
			invocation.config = jerseyConfig;
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.lastCommitId = config.getLastCommitId();
			invocation.fetchData = fetchData;
			invocation.mergedData = mergedData;
			config.setLastCommitId(invocation.execute());
		});
	}

	public void checkout(String commitId, FetchNotifier notifier) throws WebRequestException {
		if (commitId == null)
			return;
		executeLoggedIn(() -> {
			CheckoutInvocation invocation = new CheckoutInvocation(config.database, notifier);
			invocation.config = jerseyConfig;
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.commitId = commitId;
			invocation.execute();
			config.setLastCommitId(commitId);
		});
	}

	public JsonObject getDataset(ModelType type, String refId) throws WebRequestException {
		return getDataset(type, refId, null);
	}

	public JsonObject getDataset(ModelType type, String refId, String commitId) throws WebRequestException {
		return executeLoggedIn(() -> {
			DatasetContentInvocation invocation = new DatasetContentInvocation();
			invocation.config = jerseyConfig;
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.type = type;
			invocation.refId = refId;
			invocation.commitId = commitId;
			return invocation.execute();
		});
	}

	private void executeLoggedIn(Invocation runnable) throws WebRequestException {
		if (sessionId == null && config.credentials != null)
			if (!login())
				return;
		try {
			runnable.run();
		} catch (WebRequestException e) {
			if (e.getErrorCode() == Status.UNAUTHORIZED.getStatusCode() && config.credentials != null) {
				login();
				runnable.run();
			} else
				throw e;
		}
	}

	private <T> T executeLoggedIn(InvocationWithResult<T> runnable) throws WebRequestException {
		if (sessionId == null && config.credentials != null)
			if (!login())
				return null;
		try {
			return runnable.run();
		} catch (WebRequestException e) {
			if (e.getErrorCode() == Status.UNAUTHORIZED.getStatusCode() && config.credentials != null) {
				login();
				return runnable.run();
			} else
				throw e;
		}
	}

	private ClientConfig createJerseyConfig() {
		ClientConfig config = new DefaultClientConfig();
		InputStream certificateStream = this.config.getCertificate();
		if (certificateStream == null)
			return config;
		try {
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate) factory.generateCertificate(certificateStream);
			TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null);
			keyStore.setCertificateEntry("certificate", certificate);
			tmFactory.init(keyStore);
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmFactory.getTrustManagers(), null);
			config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
					new HTTPSProperties(HttpsURLConnection.getDefaultHostnameVerifier(), sslContext));
			return config;
		} catch (Exception e) {
			log.error("Error creating jersey config", e);
			return config;
		}
	}

	private interface Invocation {
		public void run() throws WebRequestException;
	}

	private interface InvocationWithResult<T> {
		public T run() throws WebRequestException;
	}

}
