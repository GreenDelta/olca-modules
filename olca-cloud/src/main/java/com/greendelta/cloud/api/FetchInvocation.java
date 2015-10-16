package com.greendelta.cloud.api;

import java.util.List;

import org.openlca.core.database.ActorDao;
import org.openlca.core.database.CategorizedEntityDao;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.CostCategoryDao;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.database.SocialIndicatorDao;
import org.openlca.core.database.SourceDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.jsonld.EntityStore;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.greendelta.cloud.model.data.FetchData;
import com.greendelta.cloud.model.data.FetchResponse;
import com.greendelta.cloud.util.Strings;
import com.greendelta.cloud.util.Valid;
import com.greendelta.cloud.util.WebRequests;
import com.greendelta.cloud.util.WebRequests.Type;
import com.greendelta.cloud.util.WebRequests.WebRequestException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Invokes a web service call to fetch the latest changes after the specified
 * commit id
 */
class FetchInvocation {

	private static final String PATH = "/repository/fetch/";

	private String baseUrl;
	private String sessionId;
	private String repositoryId;
	private String latestCommitId;
	private IDatabase database;
	private EntityStore store;

	public FetchInvocation(IDatabase database) {
		this.database = database;
		this.store = new InMemoryStore();

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

	public void setLatestCommitId(String latestCommitId) {
		this.latestCommitId = latestCommitId;
	}

	/**
	 * Retrieves all changed data sets since the last fetch
	 * 
	 * @return The latest commit id
	 * @throws WebRequestException
	 *             If user is out of sync or has no access to the specified
	 *             repository
	 */
	public String execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (latestCommitId == null || latestCommitId.isEmpty())
			latestCommitId = "null";
		String url = Strings.concat(baseUrl, PATH, repositoryId, "/",
				latestCommitId);
		ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return null;
		FetchResponse result = new Gson().fromJson(
				response.getEntity(String.class), FetchResponse.class);
		process(result.getData());
		runImport();
		return result.getLatestCommitId();
	}

	private void process(List<FetchData> input) {
		for (FetchData data : input) {
			if (data.isDeleted()) {
				delete(createDao(data.getType()), data.getRefId());
				continue;
			}
			JsonElement element = new Gson().fromJson(data.getJson(),
					JsonElement.class);
			store.put(data.getType(), element.getAsJsonObject());
		}
	}

	private void runImport() {
		JsonImport jsonImport = new JsonImport(store, database);
		jsonImport.setUpdateMode(UpdateMode.ALWAYS);
		jsonImport.run();
	}

	private <T extends CategorizedEntity, V extends CategorizedDescriptor> void delete(
			CategorizedEntityDao<T, V> dao, String refId) {
		dao.delete(dao.getForRefId(refId));
	}

	private CategorizedEntityDao<?, ?> createDao(ModelType type) {
		switch (type) {
		case ACTOR:
			return new ActorDao(database);
		case COST_CATEGORY:
			return new CostCategoryDao(database);
		case CURRENCY:
			return new CurrencyDao(database);
		case FLOW:
			return new FlowDao(database);
		case FLOW_PROPERTY:
			return new FlowPropertyDao(database);
		case IMPACT_METHOD:
			return new ImpactMethodDao(database);
		case PROCESS:
			return new ProcessDao(database);
		case PRODUCT_SYSTEM:
			return new ProductSystemDao(database);
		case PROJECT:
			return new ProjectDao(database);
		case SOCIAL_INDICATOR:
			return new SocialIndicatorDao(database);
		case SOURCE:
			return new SourceDao(database);
		case UNIT_GROUP:
			return new UnitGroupDao(database);
		case LOCATION:
			return new LocationDao(database);
		case PARAMETER:
			return new ParameterDao(database);
		case CATEGORY:
			return new CategoryDao(database);
		default:
			return null;
		}
	}

}
