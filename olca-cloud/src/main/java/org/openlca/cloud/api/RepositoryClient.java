package org.openlca.cloud.api;

import java.io.File;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.cloud.model.Announcement;
import org.openlca.cloud.model.Comment;
import org.openlca.cloud.model.Comments;
import org.openlca.cloud.model.LibraryRestriction;
import org.openlca.cloud.model.data.Commit;
import org.openlca.cloud.model.data.Dataset;
import org.openlca.cloud.model.data.DatasetEntry;
import org.openlca.cloud.model.data.FetchRequestData;
import org.openlca.cloud.model.data.FileReference;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.core.model.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.greendelta.search.wrapper.SearchResult;
import com.sun.jersey.api.client.ClientResponse.Status;

public class RepositoryClient {

	private static final Logger log = LoggerFactory.getLogger(RepositoryClient.class);
	public static final String API_VERSION = "1.3.0";
	private final RepositoryConfig config;
	private String sessionId;

	public RepositoryClient(RepositoryConfig config) {
		this.config = config;
	}

	public RepositoryConfig getConfig() {
		return config;
	}

	private boolean login() throws WebRequestException {
		LoginInvocation invocation = new LoginInvocation();
		invocation.baseUrl = config.baseUrl;
		invocation.credentials = config.credentials;
		sessionId = invocation.execute();
		return sessionId != null;
	}

	public void logout() throws WebRequestException {
		if (sessionId == null)
			return;
		LogoutInvocation invocation = new LogoutInvocation();
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
		try {
			List<Commit> result = executeLoggedIn(() -> {
				HistoryInvocation invocation = new HistoryInvocation();
				invocation.baseUrl = config.baseUrl;
				invocation.sessionId = sessionId;
				invocation.repositoryId = config.repositoryId;
				return invocation.execute();
			});
			if (result == null)
				return new ArrayList<>();
			return result;
		} catch (WebRequestException e) {
			if (e.isConnectException())
				return new ArrayList<>();
			throw e;
		}
	}

	public List<Commit> fetchNewCommitHistory() throws WebRequestException {
		List<Commit> result = executeLoggedIn(() -> {
			HistoryInvocation invocation = new HistoryInvocation();
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

	public List<LibraryRestriction> performLibraryCheck(Set<Dataset> datasets) throws WebRequestException {
		List<LibraryRestriction> result = executeLoggedIn(() -> {
			LibraryCheckInvocation invocation = new LibraryCheckInvocation();
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.datasets = datasets;
			return invocation.execute();
		});
		if (result == null)
			return new ArrayList<>();
		return result;
	}

	public List<Comment> getAllComments() throws WebRequestException {
		try {
			return executeLoggedIn(() -> {
				CommentsInvocation invocation = new CommentsInvocation();
				invocation.baseUrl = config.baseUrl;
				invocation.sessionId = sessionId;
				invocation.repositoryId = config.repositoryId;
				return invocation.execute();
			});
		} catch (WebRequestException e) {
			if (e.isConnectException())
				return new ArrayList<>();
			throw e;
		}
	}

	public Comments getComments(ModelType type, String refId) throws WebRequestException {
		try {
			return executeLoggedIn(() -> {
				CommentsInvocation invocation = new CommentsInvocation();
				invocation.baseUrl = config.baseUrl;
				invocation.sessionId = sessionId;
				invocation.repositoryId = config.repositoryId;
				invocation.type = type;
				invocation.refId = refId;
				return new Comments(invocation.execute());
			});
		} catch (WebRequestException e) {
			if (e.isConnectException())
				return new Comments(new ArrayList<>());
			throw e;
		}
	}

	public List<FetchRequestData> getReferences(String commitId) throws WebRequestException {
		List<FetchRequestData> result = executeLoggedIn(() -> {
			ReferencesInvocation invocation = new ReferencesInvocation();
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
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.lastCommitId = config.getLastCommitId();
			invocation.sync = false;
			return invocation.execute();
		});
		if (result == null)
			return new HashSet<>();
		return result;
	}

	public Set<FetchRequestData> list() throws WebRequestException {
		return sync(null);
	}

	public List<String> listRepositories() throws WebRequestException {
		return executeLoggedIn(() -> {
			ListRepositoriesInvocation invocation = new ListRepositoriesInvocation();
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			return invocation.execute();
		});
	}

	public Set<FetchRequestData> sync(String untilCommitId) throws WebRequestException {
		Set<FetchRequestData> result = executeLoggedIn(() -> {
			FetchRequestInvocation invocation = new FetchRequestInvocation();
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.lastCommitId = untilCommitId;
			invocation.sync = true;
			return invocation.execute();
		});
		if (result == null)
			return new HashSet<>();
		return result;
	}

	public SearchResult<DatasetEntry> search(String query, int page, int pageSize, ModelType type) throws WebRequestException {
		return executeLoggedIn(() -> {
			SearchInvocation invocation = new SearchInvocation();
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.query = query;
			invocation.page = page;
			invocation.pageSize = pageSize;
			invocation.type = type;
			invocation.repositoryId = getConfig().repositoryId;
			return invocation.execute();
		});
	}

	public File downloadJson(Set<FileReference> requestData) throws WebRequestException {
		return executeLoggedIn(() -> {
			DownloadJsonInvocation invocation = new DownloadJsonInvocation();
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
			FetchInvocation invocation = new FetchInvocation(config.database, notifier);
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.commitId = commitId;
			invocation.requestData = requestData != null ? requestData : new HashSet<>();
			invocation.download = true;
			invocation.clearDatabase = false;
			invocation.execute();
		});
	}

	public void fetch(Set<FileReference> fetchData, Map<Dataset, JsonObject> mergedData, FetchNotifier notifier)
			throws WebRequestException {
		executeLoggedIn(() -> {
			FetchInvocation invocation = new FetchInvocation(config.database, notifier);
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.commitId = config.getLastCommitId();
			invocation.requestData = fetchData != null ? fetchData : new HashSet<>();
			invocation.mergedData = mergedData;
			invocation.download = false;
			invocation.clearDatabase = false;
			config.setLastCommitId(invocation.execute());
		});
	}

	public void checkout(String commitId, FetchNotifier notifier) throws WebRequestException {
		if (commitId == null)
			return;
		executeLoggedIn(() -> {
			FetchInvocation invocation = new FetchInvocation(config.database, notifier);
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.commitId = commitId;
			invocation.clearDatabase = true;
			invocation.download = true;
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
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			invocation.type = type;
			invocation.refId = refId;
			invocation.commitId = commitId;
			return invocation.execute();
		});
	}

	public Announcement getAnnouncement() throws WebRequestException {
		return executeLoggedIn(() -> {
			AnnouncementInvocation invocation = new AnnouncementInvocation();
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			return invocation.execute();
		});
	}

	public InputStream export() throws WebRequestException {
		return executeLoggedIn(() -> {
			ExportRepositoryInvocation invocation = new ExportRepositoryInvocation();
			invocation.baseUrl = config.baseUrl;
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.repositoryId;
			return invocation.execute();
		});
	}

	private void executeLoggedIn(Invocation runnable) throws WebRequestException {
		if (sessionId == null && config.credentials != null)
			try {
				if (!login())
					return;
			} catch (WebRequestException e) {
				if (e.getCause() instanceof ConnectException) {
					log.warn("Could not connect to repository server " + config.getServerUrl() + ", " + e.getMessage());
				}
				throw e;
			}
		try {
			runnable.run();
		} catch (WebRequestException e) {
			if (e.getErrorCode() == Status.UNAUTHORIZED.getStatusCode() && config.credentials != null) {
				login();
				runnable.run();
			} else {
				if (e.getCause() instanceof ConnectException) {
					log.warn("Could not connect to repository server " + config.getServerUrl() + ", " + e.getMessage());
				}
				throw e;
			}
		}
	}

	private <T> T executeLoggedIn(InvocationWithResult<T> runnable) throws WebRequestException {
		if (sessionId == null && config.credentials != null)
			try {
				if (!login())
					return null;
			} catch (WebRequestException e) {
				if (e.isConnectException()) {
					log.warn("Could not connect to repository server " + config.getServerUrl() + ", " + e.getMessage());
				}
				throw e;
			}
		try {
			return runnable.run();
		} catch (WebRequestException e) {
			if (e.getErrorCode() == Status.UNAUTHORIZED.getStatusCode() && config.credentials != null) {
				login();
				return runnable.run();
			} else {
				if (e.isConnectException()) {
					log.warn("Could not connect to repository server " + config.getServerUrl() + ", " + e.getMessage());
				}
				throw e;
			}
		}
	}

	private interface Invocation {
		public void run() throws WebRequestException;
	}

	private interface InvocationWithResult<T> {
		public T run() throws WebRequestException;
	}

}
