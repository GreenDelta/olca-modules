package org.openlca.io.refdata;

import java.util.ArrayList;

import org.openlca.core.model.UnitGroup;

class UnitGroupExport implements Runnable {

	private final ExportConfig config;

	UnitGroupExport(ExportConfig config) {
		this.config = config;
	}

	@Override
	public void run() {
		var groups = config.db().getAll(UnitGroup.class);
		config.sort(groups);
		var buffer = new ArrayList<>(6);

		// write unit groups
		config.writeTo("unit_groups.csv", csv -> {
			for (var group : groups) {
				buffer.add(group.refId);
				buffer.add(group.name);
				buffer.add(group.description);
				buffer.add(config.toPath(group.category));

				var defaultProp = group.defaultFlowProperty != null
						? group.defaultFlowProperty.name
						: "";
				buffer.add(defaultProp);

				var refUnit = group.referenceUnit != null
						? group.referenceUnit.name
						: "";
				buffer.add(refUnit);

				csv.printRecord(buffer);
				buffer.clear();
			}
		});

		// write units
		config.writeTo("units.csv", csv -> {
			for (var group : groups) {
				config.sort(group.units);
				for (var unit : group.units) {
					buffer.add(unit.refId);
					buffer.add(unit.name);
					buffer.add(unit.description);
					buffer.add(unit.conversionFactor);
					buffer.add(unit.synonyms);
					buffer.add(group.name);
					csv.printRecord(buffer);
					buffer.clear();
				}
			}
		});
	}
}
