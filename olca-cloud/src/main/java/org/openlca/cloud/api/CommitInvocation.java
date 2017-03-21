package org.openlca.cloud.api;

import java.util.List;

import org.openlca.cloud.api.data.CommitInputStream;
import org.openlca.cloud.model.data.Dataset;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.core.database.IDatabase;

/**
 * Invokes a web service call to commit data to a repository
 */
public class CommitInvocation {

	private static final String PATH = "/commit/";
	private IDatabase database;
	String baseUrl;
	String sessionId;
	String repositoryId;
	String lastCommitId;
	String message;
	List<Dataset> data;

	CommitInvocation(IDatabase database) {
		this.database = database;
	}

	/**
	 * Commits the specified data to the the specified repository
	 * 
	 * @return The id of the commit
	 * @throws WebRequestException
	 *             if user is not in sync with the repository or has no access
	 *             rights to the specified repository. To check if the user is
	 *             in sync, the latest commit id (that id of the last commit
	 *             that was fetched) is send along with the request. If it does
	 *             not match the latest commit id in the repository, the user is
	 *             out of sync
	 */
	String execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		Valid.checkNotEmpty(message, "message");
		Valid.checkNotEmpty(data, "data");
		if (lastCommitId == null)
			lastCommitId = "null";
		String url = baseUrl + PATH + repositoryId + "/" + lastCommitId;
		String commitId = WebRequests.call(Type.POST, url, sessionId, new CommitInputStream(database, message, data))
				.getEntity(
						String.class);
		return commitId;
	}

}
