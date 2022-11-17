package org.openlca.io.refdata;

import java.util.ArrayList;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;

class FlowPropertyExport implements Runnable {

	private final ExportConfig config;

	FlowPropertyExport(ExportConfig config) {
		this.config = config;
	}

	@Override
	public void run() {
		var properties = config.db().getAll(FlowProperty.class);
		if (properties.isEmpty())
			return;
		config.sort(properties);
		var buffer = new ArrayList<>(6);

		config.writeTo("flow_properties.csv", csv -> {
			for (var property : properties) {
				buffer.add(property.refId);
				buffer.add(property.name);
				buffer.add(property.description);
				buffer.add(config.toPath(property.category));

				var unitGroup = property.unitGroup != null
						? property.unitGroup.name
						: "";
				buffer.add(unitGroup);

				var propType = property.flowPropertyType == FlowPropertyType.ECONOMIC
						? "economic"
						: "physical";
				buffer.add(propType);

				csv.printRecord(buffer);
				buffer.clear();
			}
		});
	}

}
