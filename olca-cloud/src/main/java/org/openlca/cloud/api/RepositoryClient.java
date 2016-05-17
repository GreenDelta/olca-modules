package org.openlca.cloud.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.cloud.model.data.Commit;
import org.openlca.cloud.model.data.Dataset;
import org.openlca.cloud.model.data.FetchRequestData;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.core.model.ModelType;

import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse.Status;

public class RepositoryClient {

	private final RepositoryConfig config;
	private String sessionId;

	public RepositoryClient(RepositoryConfig config) {
		this.config = config;
	}

	public RepositoryConfig getConfig() {
		return config;
	}

	private void login() throws WebRequestException {
		LoginInvocation invocation = new LoginInvocation();
		invocation.baseUrl = config.getBaseUrl();
		invocation.username = config.getUsername();
		invocation.password = config.getPassword();
		sessionId = invocation.execute();
	}

	public void logout() throws WebRequestException {
		if (sessionId == null)
			return;
		LogoutInvocation invocation = new LogoutInvocation();
		invocation.baseUrl = config.getBaseUrl();
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
		return executeLoggedIn(() -> {
			CheckAccessInvocation invocation = new CheckAccessInvocation();
			invocation.baseUrl = config.getBaseUrl();
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
	}

	public boolean requestCommit() throws WebRequestException {
		return executeLoggedIn(() -> {
			CommitRequestInvocation invocation = new CommitRequestInvocation();
			invocation.baseUrl = config.getBaseUrl();
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.getRepositoryId();
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
	}

	public CommitInvocation createCommitInvocation() {
		CommitInvocation invocation = new CommitInvocation(config.getDatabase());
		invocation.baseUrl = config.getBaseUrl();
		invocation.sessionId = sessionId;
		invocation.repositoryId = config.getRepositoryId();
		invocation.lastCommitId = config.getLastCommitId();
		return invocation;
	}

	public void execute(CommitInvocation invocation) throws WebRequestException {
		executeLoggedIn(() -> {
			config.setLastCommitId(invocation.execute());
		});
	}

	public List<Commit> fetchCommitHistory() throws WebRequestException {
		return executeLoggedIn(() -> {
			HistoryInvocation invocation = new HistoryInvocation();
			invocation.baseUrl = config.getBaseUrl();
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.getRepositoryId();
			return invocation.execute();
		});
	}

	public List<Commit> fetchNewCommitHistory() throws WebRequestException {
		return executeLoggedIn(() -> {
			HistoryInvocation invocation = new HistoryInvocation();
			invocation.baseUrl = config.getBaseUrl();
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.getRepositoryId();
			invocation.lastCommitId = config.getLastCommitId();
			return invocation.execute();
		});
	}

	public Map<Dataset, String> performLibraryCheck(Set<Dataset> datasets)
			throws WebRequestException {
		return executeLoggedIn(() -> {
			LibraryCheckInvocation invocation = new LibraryCheckInvocation();
			invocation.baseUrl = config.getBaseUrl();
			invocation.sessionId = sessionId;
			invocation.datasets = datasets;
			return invocation.execute();
		});
	}

	public List<FetchRequestData> getReferences(String commitId)
			throws WebRequestException {
		return executeLoggedIn(() -> {
			ReferencesInvocation invocation = new ReferencesInvocation();
			invocation.baseUrl = config.getBaseUrl();
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.getRepositoryId();
			invocation.commitId = commitId;
			return invocation.execute();
		});
	}

	public List<FetchRequestData> requestFetch() throws WebRequestException {
		return executeLoggedIn(() -> {
			FetchRequestInvocation invocation = new FetchRequestInvocation();
			invocation.baseUrl = config.getBaseUrl();
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.getRepositoryId();
			invocation.lastCommitId = config.getLastCommitId();
			return invocation.execute();
		});
	}

	public void fetch(List<Dataset> fetchData,
			Map<Dataset, JsonObject> mergedData) throws WebRequestException {
		executeLoggedIn(() -> {
			FetchInvocation invocation = new FetchInvocation(config.getDatabase());
			invocation.baseUrl = config.getBaseUrl();
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.getRepositoryId();
			invocation.lastCommitId = config.getLastCommitId();
			invocation.fetchData = fetchData;
			invocation.mergedData = mergedData;
			config.setLastCommitId(invocation.execute());
		});
	}

	public void checkout(String commitId) throws WebRequestException {
		if (commitId == null)
			return;
		executeLoggedIn(() -> {
			CheckoutInvocation invocation = new CheckoutInvocation(config.getDatabase());
			invocation.baseUrl = config.getBaseUrl();
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.getRepositoryId();
			invocation.commitId = commitId;
			invocation.execute();
			config.setLastCommitId(commitId);
		});
	}

	public JsonObject getDataset(ModelType type, String refId)
			throws WebRequestException {
		return getDataset(type, refId, null);
	}

	public JsonObject getDataset(ModelType type, String refId, String commitId)
			throws WebRequestException {
		return executeLoggedIn(() -> {
			DatasetContentInvocation invocation = new DatasetContentInvocation();
			invocation.baseUrl = config.getBaseUrl();
			invocation.sessionId = sessionId;
			invocation.repositoryId = config.getRepositoryId();
			invocation.type = type;
			invocation.refId = refId;
			invocation.commitId = commitId;
			return invocation.execute();
		});
	}

	private void executeLoggedIn(Invocation runnable)
			throws WebRequestException {
		if (sessionId == null)
			login();
		try {
			runnable.run();
		} catch (WebRequestException e) {
			if (e.getErrorCode() == Status.UNAUTHORIZED.getStatusCode()) {
				login();
				runnable.run();
			} else
				throw e;
		}
	}

	private <T> T executeLoggedIn(InvocationWithResult<T> runnable)
			throws WebRequestException {
		if (sessionId == null)
			login();
		try {
			return runnable.run();
		} catch (WebRequestException e) {
			if (e.getErrorCode() == Status.UNAUTHORIZED.getStatusCode()) {
				login();
				return runnable.run();
			} else
				throw e;
		}
	}

	private interface Invocation {
		public void run() throws WebRequestException;
	}

	private interface InvocationWithResult<T> {
		public T run() throws WebRequestException;
	}

}
