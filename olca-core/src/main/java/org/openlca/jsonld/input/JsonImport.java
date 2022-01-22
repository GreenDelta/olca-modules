package org.openlca.jsonld.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ExchangeProviderQueue;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.JsonStoreReader;

public class JsonImport implements Runnable {

	final JsonStoreReader reader;
	UpdateMode updateMode = UpdateMode.NEVER;
	private Consumer<RootEntity> callback;

	@Deprecated
	final Db db;
	private final ExchangeProviderQueue providers;
	private final Map<ModelType, Set<String>> visited = new HashMap<>();

	public JsonImport(JsonStoreReader reader, IDatabase db) {
		this.reader = reader;
		this.db = new Db(db);
		this.providers = ExchangeProviderQueue.create(db);
	}

	public JsonImport setUpdateMode(UpdateMode updateMode) {
		this.updateMode = updateMode;
		return this;
	}

	public JsonImport setCallback(Consumer<RootEntity> callback) {
		this.callback = callback;
		return this;
	}

	void visited(ModelType type, String refId) {
		var set = visited.computeIfAbsent(type, k -> new HashSet<>());
		set.add(refId);
	}

	public ExchangeProviderQueue providers() {
		return providers;
	}

	void imported(RootEntity entity) {
		if (callback == null)
			return;
		callback.accept(entity);
	}

	boolean hasVisited(ModelType type, String refId) {
		Set<String> set = visited.get(type);
		return set != null && set.contains(refId);
	}

	public void run(ModelType type, String id) {
		if (type == null || id == null)
			return;
		switch (type) {
			case CATEGORY -> CategoryImport.run(id, this);
			case DQ_SYSTEM -> DQSystemImport.run(id, this);
			case LOCATION -> LocationImport.run(id, this);
			case ACTOR -> ActorImport.run(id, this);
			case SOURCE -> SourceImport.run(id, this);
			case PARAMETER -> ParameterImport.run(id, this);
			case UNIT_GROUP -> UnitGroupImport.run(id, this);
			case FLOW_PROPERTY -> FlowPropertyImport.run(id, this);
			case CURRENCY -> CurrencyImport.run(id, this);
			case FLOW -> FlowImport.run(id, this);
			case IMPACT_METHOD -> ImpactMethodImport.run(id, this);
			case IMPACT_CATEGORY -> ImpactCategoryImport.run(id, this);
			case SOCIAL_INDICATOR -> SocialIndicatorImport.run(id, this);
			case PROCESS -> ProcessImport.run(id, this);
			case PRODUCT_SYSTEM -> ProductSystemImport.run(id, this);
			case PROJECT -> ProjectImport.run(id, this);
			default -> {
			}
		}
	}

	@Override
	public void run() {
		for (String catId : reader.getRefIds(ModelType.CATEGORY))
			CategoryImport.run(catId, this);
		for (String sysId : reader.getRefIds(ModelType.DQ_SYSTEM))
			DQSystemImport.run(sysId, this);
		for (String locId : reader.getRefIds(ModelType.LOCATION))
			LocationImport.run(locId, this);
		for (String actorId : reader.getRefIds(ModelType.ACTOR))
			ActorImport.run(actorId, this);
		for (String sourceId : reader.getRefIds(ModelType.SOURCE))
			SourceImport.run(sourceId, this);
		for (String paramId : reader.getRefIds(ModelType.PARAMETER))
			ParameterImport.run(paramId, this);
		for (String groupId : reader.getRefIds(ModelType.UNIT_GROUP))
			UnitGroupImport.run(groupId, this);
		for (String propId : reader.getRefIds(ModelType.FLOW_PROPERTY))
			FlowPropertyImport.run(propId, this);
		for (String currId : reader.getRefIds(ModelType.CURRENCY))
			CurrencyImport.run(currId, this);
		for (String flowId : reader.getRefIds(ModelType.FLOW))
			FlowImport.run(flowId, this);
		for (String id : reader.getRefIds(ModelType.IMPACT_CATEGORY))
			ImpactCategoryImport.run(id, this);
		for (String methodId : reader.getRefIds(ModelType.IMPACT_METHOD))
			ImpactMethodImport.run(methodId, this);
		for (String indicatorId : reader.getRefIds(ModelType.SOCIAL_INDICATOR))
			SocialIndicatorImport.run(indicatorId, this);
		for (String processId : reader.getRefIds(ModelType.PROCESS))
			ProcessImport.run(processId, this);
		for (String systemId : reader.getRefIds(ModelType.PRODUCT_SYSTEM))
			ProductSystemImport.run(systemId, this);
		for (String projectId : reader.getRefIds(ModelType.PROJECT))
			ProjectImport.run(projectId, this);
	}


}
