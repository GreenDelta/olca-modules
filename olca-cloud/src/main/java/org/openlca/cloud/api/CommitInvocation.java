package org.openlca.cloud.api;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.cloud.api.data.CommitStream;
import org.openlca.cloud.model.data.Dataset;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.NwSetDescriptor;

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
	Set<Dataset> data;

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
	String execute(Consumer<Dataset> callback) throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(repositoryId, "repository id");
		Valid.checkNotEmpty(message, "message");
		Valid.checkNotEmpty(data, "data");
		String url = baseUrl + PATH + repositoryId;
		if (lastCommitId != null) {
			url += "?lastCommitId=" + lastCommitId;
		}
		ImpactMethodDao dao = new ImpactMethodDao(database);
		for (Dataset ds : new ArrayList<>(data)) {
			if (ds.type == ModelType.IMPACT_METHOD) {
				for (ImpactCategoryDescriptor cat : dao.getCategoryDescriptors(ds.refId)) {
					data.add(toDataset(cat));
				}
				for (NwSetDescriptor nwSet : dao.getNwSetDescriptors(ds.refId)) {
					data.add(toDataset(nwSet));
				}
			}
		}
		String commitId = WebRequests.call(Type.POST, url, sessionId,
				new CommitStream(database, message, data, callback))
				.getEntity(String.class);
		return commitId;
	}

	private Dataset toDataset(Descriptor descriptor) {
		Dataset ds = new Dataset();
		ds.refId = descriptor.refId;
		ds.type = descriptor.type;
		ds.version = Version.asString(descriptor.version);
		ds.lastChange = descriptor.lastChange;
		ds.name = descriptor.name;
		ds.categoryType = descriptor.type;
		return ds;
	}

}
