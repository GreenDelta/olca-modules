package org.openlca.jsonld.input;

import java.util.Objects;
import com.google.gson.JsonObject;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowPropertyImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private String refId;
	private ImportConfig conf;

	private FlowPropertyImport(String refId, ImportConfig conf) {
		this.refId = refId;
		this.conf = conf;
	}

	static FlowProperty run(String refId, ImportConfig conf) {
		return new FlowPropertyImport(refId, conf).run();
	}

	private FlowProperty run() {
		if (refId == null || conf == null)
			return null;
		try {
			FlowProperty p = conf.db.getFlowProperty(refId);
			if (p != null)
				return p;
			JsonObject json = conf.store.get(ModelType.FLOW_PROPERTY, refId);
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
		p.setCategory(CategoryImport.run(catId, conf));
		mapType(json, p);
		String unitGroupId = In.getRefId(json, "unitGroup");
		p.setUnitGroup(UnitGroupImport.run(unitGroupId, conf));
		return conf.db.put(p);
	}

	private void mapType(JsonObject json, FlowProperty p) {
		String typeString = In.getString(json, "flowPropertyType");
		if (Objects.equals(typeString, "ECONOMIC_QUANTITY"))
			p.setFlowPropertyType(FlowPropertyType.ECONOMIC);
		else
			p.setFlowPropertyType(FlowPropertyType.PHYSICAL);
	}
}
