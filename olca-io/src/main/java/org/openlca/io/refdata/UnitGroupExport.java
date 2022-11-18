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
		if (groups.isEmpty())
			return;
		config.sort(groups);
		var buffer = new ArrayList<>(6);

		// write unit groups
		config.writeTo("unit_groups.csv", csv -> {

			// write column headers
			csv.printRecord(
					"ID",
					"Name",
					"Description",
					"Category",
					"Default flow property",
					"Reference unit");

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

			// write column headers
			csv.printRecord(
					"ID",
					"Name",
					"Description",
					"Conversion factor",
					"Synonyms",
					"Unit group");

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
