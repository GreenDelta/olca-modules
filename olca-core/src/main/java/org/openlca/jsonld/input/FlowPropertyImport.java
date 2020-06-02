package org.openlca.jsonld.input;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

class FlowPropertyImport extends BaseImport<FlowProperty> {

	private FlowPropertyImport(String refId, ImportConfig conf) {
		super(ModelType.FLOW_PROPERTY, refId, conf);
	}

	static FlowProperty run(String refId, ImportConfig conf) {
		return new FlowPropertyImport(refId, conf).run();
	}

	@Override
	FlowProperty map(JsonObject json, long id) {
		if (json == null)
			return null;
		FlowProperty p = new FlowProperty();
		In.mapAtts(json, p, id, conf);
		p.flowPropertyType = Json.getEnum(json, "flowPropertyType",
		FlowPropertyType.class);
		String unitGroupId = Json.getRefId(json, "unitGroup");
		p.unitGroup = UnitGroupImport.run(unitGroupId, conf);
		return conf.db.put(p);
	}

}
