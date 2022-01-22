package org.openlca.jsonld.input;

import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.JsonStoreReader;

public class JsonImport implements Runnable {

	private final IDatabase database;
	private final JsonStoreReader reader;
	private UpdateMode updateMode = UpdateMode.NEVER;
	private Consumer<RootEntity> callback;

	public JsonImport(JsonStoreReader reader, IDatabase db) {
		this.reader = reader;
		this.database = db;
	}

	public JsonImport setUpdateMode(UpdateMode updateMode) {
		this.updateMode = updateMode;
		return this;
	}

	public JsonImport setCallback(Consumer<RootEntity> callback) {
		this.callback = callback;
		return this;
	}

	public void run(ModelType type, String id) {
		if (type == null || id == null)
			return;
		ImportConfig conf = ImportConfig.create(
				new Db(database), reader, updateMode, callback);
		switch (type) {
			case CATEGORY -> CategoryImport.run(id, conf);
			case DQ_SYSTEM -> DQSystemImport.run(id, conf);
			case LOCATION -> LocationImport.run(id, conf);
			case ACTOR -> ActorImport.run(id, conf);
			case SOURCE -> SourceImport.run(id, conf);
			case PARAMETER -> ParameterImport.run(id, conf);
			case UNIT_GROUP -> UnitGroupImport.run(id, conf);
			case FLOW_PROPERTY -> FlowPropertyImport.run(id, conf);
			case CURRENCY -> CurrencyImport.run(id, conf);
			case FLOW -> FlowImport.run(id, conf);
			case IMPACT_METHOD -> ImpactMethodImport.run(id, conf);
			case IMPACT_CATEGORY -> ImpactCategoryImport.run(id, conf);
			case SOCIAL_INDICATOR -> SocialIndicatorImport.run(id, conf);
			case PROCESS -> ProcessImport.run(id, conf);
			case PRODUCT_SYSTEM -> ProductSystemImport.run(id, conf);
			case PROJECT -> ProjectImport.run(id, conf);
			default -> {
			}
		}
	}

	@Override
	public void run() {
		var conf = ImportConfig.create(new Db(database), reader, updateMode, callback);
		for (String catId : reader.getRefIds(ModelType.CATEGORY))
			CategoryImport.run(catId, conf);
		for (String sysId : reader.getRefIds(ModelType.DQ_SYSTEM))
			DQSystemImport.run(sysId, conf);
		for (String locId : reader.getRefIds(ModelType.LOCATION))
			LocationImport.run(locId, conf);
		for (String actorId : reader.getRefIds(ModelType.ACTOR))
			ActorImport.run(actorId, conf);
		for (String sourceId : reader.getRefIds(ModelType.SOURCE))
			SourceImport.run(sourceId, conf);
		for (String paramId : reader.getRefIds(ModelType.PARAMETER))
			ParameterImport.run(paramId, conf);
		for (String groupId : reader.getRefIds(ModelType.UNIT_GROUP))
			UnitGroupImport.run(groupId, conf);
		for (String propId : reader.getRefIds(ModelType.FLOW_PROPERTY))
			FlowPropertyImport.run(propId, conf);
		for (String currId : reader.getRefIds(ModelType.CURRENCY))
			CurrencyImport.run(currId, conf);
		for (String flowId : reader.getRefIds(ModelType.FLOW))
			FlowImport.run(flowId, conf);
		for (String id : reader.getRefIds(ModelType.IMPACT_CATEGORY))
			ImpactCategoryImport.run(id, conf);
		for (String methodId : reader.getRefIds(ModelType.IMPACT_METHOD))
			ImpactMethodImport.run(methodId, conf);
		for (String indicatorId : reader.getRefIds(ModelType.SOCIAL_INDICATOR))
			SocialIndicatorImport.run(indicatorId, conf);
		for (String processId : reader.getRefIds(ModelType.PROCESS))
			ProcessImport.run(processId, conf);
		for (String systemId : reader.getRefIds(ModelType.PRODUCT_SYSTEM))
			ProductSystemImport.run(systemId, conf);
		for (String projectId : reader.getRefIds(ModelType.PROJECT))
			ProjectImport.run(projectId, conf);
	}


}
