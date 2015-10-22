package org.openlca.jsonld.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.EntityStore;

public class JsonImport implements Runnable {

	private IDatabase database;
	private EntityStore store;
	private UpdateMode updateMode = UpdateMode.NEVER;

	public JsonImport(EntityStore store, IDatabase db) {
		this.store = store;
		this.database = db;
	}

	public void setUpdateMode(UpdateMode updateMode) {
		this.updateMode = updateMode;
	}

	@Override
	public void run() {
		ImportConfig conf = ImportConfig.create(new Db(database), store,
				updateMode);
		for (String locId : store.getRefIds(ModelType.LOCATION))
			LocationImport.run(locId, conf);
		for (String catId : store.getRefIds(ModelType.CATEGORY))
			CategoryImport.run(catId, conf);
		for (String actorId : store.getRefIds(ModelType.ACTOR))
			ActorImport.run(actorId, conf);
		for (String sourceId : store.getRefIds(ModelType.SOURCE))
			SourceImport.run(sourceId, conf);
		for (String paramId : store.getRefIds(ModelType.PARAMETER))
			ParameterImport.run(paramId, conf);
		for (String groupId : store.getRefIds(ModelType.UNIT_GROUP))
			UnitGroupImport.run(groupId, conf);
		for (String propId : store.getRefIds(ModelType.FLOW_PROPERTY))
			FlowPropertyImport.run(propId, conf);
		for (String ccId : store.getRefIds(ModelType.COST_CATEGORY))
			CostCategoryImport.run(ccId, conf);
		for (String currId : store.getRefIds(ModelType.CURRENCY))
			CurrencyImport.run(currId, conf);
		for (String flowId : store.getRefIds(ModelType.FLOW))
			FlowImport.run(flowId, conf);
		for (String methodId : store.getRefIds(ModelType.IMPACT_METHOD))
			ImpactMethodImport.run(methodId, conf);
		for (String indicatorId : store.getRefIds(ModelType.SOCIAL_INDICATOR))
			SocialIndicatorImport.run(indicatorId, conf);
		for (String processId : store.getRefIds(ModelType.PROCESS))
			ProcessImport.run(processId, conf);
	}

}
