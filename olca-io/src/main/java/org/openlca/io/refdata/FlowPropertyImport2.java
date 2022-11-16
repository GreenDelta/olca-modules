package org.openlca.io.refdata;

import java.util.ArrayList;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;
import org.openlca.util.Strings;

class FlowPropertyImport2 implements Runnable {

	private final ImportConfig config;

	FlowPropertyImport2(ImportConfig config) {
		this.config = config;
	}

	@Override
	public void run() {

		// collect and insert flow properties
		var props = new ArrayList<FlowProperty>();
		config.eachRow("flow_properties.csv", row -> {
			var group = config.get(UnitGroup.class, row.get(4));
			if (group == null) {
				config.log().error("unknown unit group: " + row.get(4));
				return;
			}
			var prop = new FlowProperty();
			prop.refId = row.get(0);
			prop.name = row.get(1);
			prop.description = row.get(2);
			prop.category = config.category(ModelType.FLOW_PROPERTY, row.get(3));
			prop.unitGroup = group;
			prop.flowPropertyType = typeOf(row.get(5));
			props.add(prop);
		});
		config.insert(props);

		// update possible default flow properties in unit groups
		config.eachRow("unit_groups.csv", row -> {
			var group = config.get(UnitGroup.class, row.get(0));
			var prop = config.get(FlowProperty.class, row.get(4));
			if (group == null || prop == null)
				return;
			group.defaultFlowProperty = prop;
			config.update(group);
			config.reload(prop);
		});
	}

	private FlowPropertyType typeOf(String propType) {
		if (Strings.nullOrEmpty(propType))
			return FlowPropertyType.PHYSICAL;
		var c = propType.trim().charAt(0);
		return c == 'e' || c == 'E'
				? FlowPropertyType.ECONOMIC
				: FlowPropertyType.PHYSICAL;
	}
}
