package org.openlca.jsonld.input;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

class FlowPropertyImport extends BaseImport<FlowProperty> {

	private FlowPropertyImport(String refId, JsonImport conf) {
		super(ModelType.FLOW_PROPERTY, refId, conf);
	}

	static FlowProperty run(String refId, JsonImport conf) {
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
		// check if the flow property was inserted during UnitGroupImport,
		// cyclic dependencies can otherwise cause duplicate entries
		var defaultProperty = p.unitGroup != null
			? p.unitGroup.defaultFlowProperty
			: null;
		
		if (defaultProperty != null
			&& p.refId != null
			&& p.refId.equals(defaultProperty.refId)
			&& conf.hasVisited(ModelType.UNIT_GROUP, unitGroupId))
			return defaultProperty;
		return conf.db.put(p);
	}

}
