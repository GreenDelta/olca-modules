package org.openlca.jsonld.input;

import java.util.Objects;
import com.google.gson.JsonObject;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.EntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowPropertyImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private String refId;
	private EntityStore store;
	private Db db;

	private FlowPropertyImport(String refId, EntityStore store, Db db) {
		this.refId = refId;
		this.store = store;
		this.db = db;
	}

	static FlowProperty run(String refId, EntityStore store, Db db) {
		return new FlowPropertyImport(refId, store, db).run();
	}

	private FlowProperty run() {
		if (refId == null || store == null || db == null)
			return null;
		try {
			FlowProperty p = db.getFlowProperty(refId);
			if (p != null)
				return p;
			JsonObject json = store.get(ModelType.FLOW_PROPERTY, refId);
			return map(json);
		} catch (Exception e) {
			log.error("failed to import flow property " + refId, e);
			return null;
		}
	}

	private FlowProperty map(JsonObject json) {
		if (json == null)
			return null;
		FlowProperty p = new FlowProperty();
		In.mapAtts(json, p);
		String catId = In.getRefId(json, "category");
		p.setCategory(CategoryImport.run(catId, store, db));
		mapType(json, p);
		String unitGroupId = In.getRefId(json, "unitGroup");
		p.setUnitGroup(UnitGroupImport.run(unitGroupId, store, db));
		return db.put(p);
	}

	private void mapType(JsonObject json, FlowProperty p) {
		String typeString = In.getString(json, "flowPropertyType");
		if (Objects.equals(typeString, "ECONOMIC_QUANTITY"))
			p.setFlowPropertyType(FlowPropertyType.ECONOMIC);
		else
			p.setFlowPropertyType(FlowPropertyType.PHYSICAL);
	}
}
