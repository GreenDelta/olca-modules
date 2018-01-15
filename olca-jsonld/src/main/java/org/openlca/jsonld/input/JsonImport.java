package org.openlca.jsonld.input;

import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.EntityStore;
import org.openlca.jsonld.Schema;
import org.openlca.jsonld.Schema.UnsupportedSchemaException;

import com.google.gson.JsonObject;

public class JsonImport implements Runnable {

	private IDatabase database;
	private EntityStore store;
	private UpdateMode updateMode = UpdateMode.NEVER;
	private Consumer<RootEntity> callback;

	public JsonImport(EntityStore store, IDatabase db) {
		this.store = store;
		this.database = db;
	}

	public void setUpdateMode(UpdateMode updateMode) {
		this.updateMode = updateMode;
	}

	public void setCallback(Consumer<RootEntity> callback) {
		this.callback = callback;
	}

	public void run(ModelType type, String id) {
		checkSchemaSupported();
		if (type == null || id == null)
			return;
		ImportConfig conf = ImportConfig.create(
				new Db(database), store, updateMode, callback);
		switch (type) {
		case CATEGORY:
			CategoryImport.run(id, conf);
			break;
		case DQ_SYSTEM:
			DQSystemImport.run(id, conf);
			break;
		case LOCATION:
			LocationImport.run(id, conf);
			break;
		case ACTOR:
			ActorImport.run(id, conf);
			break;
		case SOURCE:
			SourceImport.run(id, conf);
			break;
		case PARAMETER:
			ParameterImport.run(id, conf);
			break;
		case UNIT_GROUP:
			UnitGroupImport.run(id, conf);
			break;
		case FLOW_PROPERTY:
			FlowPropertyImport.run(id, conf);
			break;
		case CURRENCY:
			CurrencyImport.run(id, conf);
			break;
		case FLOW:
			FlowImport.run(id, conf);
			break;
		case IMPACT_METHOD:
			ImpactMethodImport.run(id, conf);
			break;
		case SOCIAL_INDICATOR:
			SocialIndicatorImport.run(id, conf);
			break;
		case PROCESS:
			ProcessImport.run(id, conf);
			break;
		case PRODUCT_SYSTEM:
			ProductSystemImport.run(id, conf);
			break;
		case PROJECT:
			ProjectImport.run(id, conf);
			break;
		default:
			break;
		}
	}

	@Override
	public void run() {
		checkSchemaSupported();
		ImportConfig conf = ImportConfig.create(new Db(database), store, updateMode, callback);
		for (String catId : store.getRefIds(ModelType.CATEGORY))
			CategoryImport.run(catId, conf);
		for (String sysId : store.getRefIds(ModelType.DQ_SYSTEM))
			DQSystemImport.run(sysId, conf);
		for (String locId : store.getRefIds(ModelType.LOCATION))
			LocationImport.run(locId, conf);
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
		for (String systemId : store.getRefIds(ModelType.PRODUCT_SYSTEM))
			ProductSystemImport.run(systemId, conf);
		for (String projectId : store.getRefIds(ModelType.PROJECT))
			ProjectImport.run(projectId, conf);
	}

	private void checkSchemaSupported() {
		JsonObject context = store.getContext();
		String schema = Schema.parseUri(context);
		if (!Schema.isSupportedSchema(schema))
			throw new UnsupportedSchemaException(schema);
	}

}
