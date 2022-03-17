package org.openlca.jsonld.input;

import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.Json;

public record FlowPropertyReader(EntityResolver resolver)
	implements EntityReader<FlowProperty> {

	public FlowPropertyReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public FlowProperty read(JsonObject json) {
		var property = new FlowProperty();
		update(property, json);
		return property;
	}

	@Override
	public void update(FlowProperty property, JsonObject json) {
		Util.mapBase(property, json, resolver);
		property.flowPropertyType = Json.getEnum(
			json, "flowPropertyType", FlowPropertyType.class);
		var groupId = Json.getRefId(json, "unitGroup");
		property.unitGroup = resolver.get(UnitGroup.class, groupId);
	}
}
