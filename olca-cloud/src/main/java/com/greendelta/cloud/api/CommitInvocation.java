package com.greendelta.cloud.api;

import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.jsonld.EntityStore;
import org.openlca.jsonld.output.JsonExport;

import com.greendelta.cloud.model.data.Commit;
import com.greendelta.cloud.model.data.CommitData;
import com.greendelta.cloud.model.data.DatasetIdentifier;
import com.greendelta.cloud.util.Strings;
import com.greendelta.cloud.util.Valid;
import com.greendelta.cloud.util.WebRequests;
import com.greendelta.cloud.util.WebRequests.Type;
import com.greendelta.cloud.util.WebRequests.WebRequestException;

/**
 * Invokes a web service call to commit data to a repository
 */
class CommitInvocation {

	private static final String PATH = "/repository/commit/";
	private Commit commit = new Commit();
	private String baseUrl;
	private String sessionId;
	private String repositoryId;
	private String latestCommitId;

	public CommitData add(CategorizedEntity entity) {
		CommitData data = new CommitData();
		DatasetIdentifier identifier = new DatasetIdentifier();
		identifier.setLastChange(entity.getLastChange());
		identifier.setRefId(entity.getRefId());
		identifier.setName(entity.getName());
		identifier.setType(ModelType.forModelClass(entity.getClass()));
		identifier.setVersion(new Version(entity.getVersion()).toString());
		if (entity.getCategory() != null)
			identifier.setCategoryRefId(entity.getCategory().getRefId());
		if (entity instanceof Category)
			identifier.setCategoryType(((Category) entity).getModelType());
		else
			identifier.setCategoryType(ModelType.forModelClass(entity
					.getClass()));
		identifier.setFullPath(getFullPath(entity));
		data.setIdentifier(identifier);
		data.setJson(toJson(entity));
		commit.getData().add(data);
		return data;
	}

	private String getFullPath(CategorizedEntity entity) {
		String path = entity.getName();
		Category category = entity.getCategory();
		while (category != null) {
			path = category + "/" + path;
			category = category.getCategory();
		}
		return path;
	}

	public CommitData addDelete(DatasetIdentifier identifier) {
		CommitData data = new CommitData();
		data.setIdentifier(identifier);
		commit.getData().add(data);
		return data;
	}

	private String toJson(CategorizedEntity entity) {
		EntityStore store = new InMemoryStore();
		ModelType type = ModelType.forModelClass(entity.getClass());
		new JsonExport(null, store).write(entity, (message, data) -> {
		});
		return store.get(type, entity.getRefId()).toString();
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public void setCommitMessage(String message) {
		commit.setMessage(message);
	}

	public void setLatestCommitId(String latestCommitId) {
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
	public String execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		Valid.checkNotEmpty(commit.getData(), "commit data");
		if (latestCommitId == null)
			latestCommitId = "null";
		if (commit.getMessage() == null)
			commit.setMessage("");
		String url = Strings.concat(baseUrl, PATH, repositoryId, "/",
				latestCommitId);
		return WebRequests.call(Type.POST, url, sessionId, commit).getEntity(
				String.class);
	}

}
