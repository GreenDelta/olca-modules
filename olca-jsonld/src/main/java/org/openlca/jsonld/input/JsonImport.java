package org.openlca.jsonld.input;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.jsonld.EntityStore;
import org.openlca.jsonld.Schema;
import org.openlca.jsonld.Schema.UnsupportedSchemaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class JsonImport implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());
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
		case IMPACT_CATEGORY:
			ImpactCategoryImport.run(id, conf);
			break;
		case SOCIAL_INDICATOR:
			SocialIndicatorImport.run(id, conf);
			break;
		case PROCESS:
			ProcessImport.run(id, conf);
			try {
				setProviders(conf);
			} catch (SQLException e) {
				log.error("Error setting providers", e);
			}
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
		for (String id : store.getRefIds(ModelType.IMPACT_CATEGORY))
			ImpactCategoryImport.run(id, conf);
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
		try {
			setProviders(conf);
			database.getEntityFactory().getCache().evictAll();
		} catch (SQLException e) {
			log.error("Error setting providers", e);
		}
	}

	private void setProviders(ImportConfig conf) throws SQLException {
		log.debug("Preparing to set providers");
		if (conf.providerInfo.isEmpty())
			return;
		Map<String, Long> refIdToId = new HashMap<>();
		Map<Long, String> idToRefId = new HashMap<>();
		for (ProcessDescriptor p : new ProcessDao(database).getDescriptors()) {
			refIdToId.put(p.refId, p.id);
			idToRefId.put(p.id, p.refId);
		}
		Stack<Long> owners = new Stack<>();
		for (String refId : conf.providerInfo.keySet()) {
			if (conf.providerInfo.get(refId).isEmpty())
				continue;
			owners.add(refIdToId.get(refId));
		}
		Set<Long> next = new HashSet<>();
		while (!owners.isEmpty()) {
			next.add(owners.pop());
			if (next.size() == 1000 || owners.isEmpty()) {
				setProviders(conf, next, refIdToId, idToRefId);
			}
		}
	}

	private void setProviders(ImportConfig conf, Set<Long> ids, Map<String, Long> refIdToId,
			Map<Long, String> idToRefId)
			throws SQLException {
		log.debug("Setting next " + ids.size() + " providers");
		Connection con = database.createConnection();
		Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = stmt.executeQuery(createProvidersQuery(ids));
		while (rs.next()) {
			long ownerId = rs.getLong("f_owner");
			String ownerRefId = idToRefId.get(ownerId);
			int internalId = rs.getInt("internal_id");
			Map<Integer, String> info = conf.providerInfo.get(ownerRefId);
			if (info == null) {
				continue;
			}
			String providerRefId = info.get(internalId);
			if (providerRefId == null) {
				continue;
			}
			Long providerId = refIdToId.get(providerRefId);
			if (providerId == null) {
				log.warn("No process found for provider ref id " + providerRefId);
				continue;
			}
			rs.updateLong("f_default_provider", providerId);
			rs.updateRow();
		}
		rs.close();
		stmt.close();
		con.commit();
		con.close();
	}

	private String createProvidersQuery(Set<Long> ids) {
		String query = "SELECT f_owner, internal_id, f_default_provider FROM tbl_exchanges WHERE f_owner IN (";
		for (long id : ids) {
			if (!query.endsWith("(")) {
				query += ",";
			}
			query += id;
		}
		return query += ")";
	}

	private void checkSchemaSupported() {
		JsonObject context = store.getContext();
		String schema = Schema.parseUri(context);
		if (!Schema.isSupportedSchema(schema))
			throw new UnsupportedSchemaException(schema);
	}

}
