package com.greendelta.cloud.api;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greendelta.cloud.util.Directories;
import com.greendelta.cloud.util.Strings;
import com.greendelta.cloud.util.Valid;
import com.greendelta.cloud.util.WebRequests;
import com.greendelta.cloud.util.WebRequests.Type;
import com.greendelta.cloud.util.WebRequests.WebRequestException;

/**
 * Invokes a web service call to commit data to a repository
 */
public class CommitInvocation extends CommitWriter {

	private static final String PATH = "/repository/commit/";
	private final Logger log = LoggerFactory.getLogger(getClass());
	private String baseUrl;
	private String sessionId;
	private String repositoryId;
	private String latestCommitId;

	CommitInvocation(IDatabase database) {
		super(database);
	}

	void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	void setLatestCommitId(String latestCommitId) {
		this.latestCommitId = latestCommitId;
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
		close();
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (latestCommitId == null)
			latestCommitId = "null";
		String url = Strings.concat(baseUrl, PATH, repositoryId, "/",
				latestCommitId);
		try {
			String commitId = WebRequests.call(Type.POST, url, sessionId,
					new FileInputStream(file)).getEntity(String.class);
			return commitId;
		} catch (FileNotFoundException e) {
			log.error("Error cleaning committing data", e);
			return null;
		} finally {
			if (file != null && file.getParentFile().exists())
				Directories.delete(file.getParentFile());
		}
	}

}
