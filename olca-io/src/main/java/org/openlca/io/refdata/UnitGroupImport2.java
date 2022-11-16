package org.openlca.io.refdata;


import java.util.HashMap;
import java.util.Objects;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

class UnitGroupImport2 implements Runnable {

	private final ImportConfig config;

	UnitGroupImport2(ImportConfig config) {
		this.config = config;
	}

	@Override
	public void run() {

		// prepare the unit groups
		var groupRefs = new HashMap<String, GroupRef>();

		config.eachRow("unit_groups.csv", row -> {
			var group = new UnitGroup();
			group.refId = row.get(0);
			group.name = row.get(1);
			group.description = row.get(2);
			group.category = config.category(ModelType.UNIT_GROUP, row.get(3));
			var ref = new GroupRef(group, row.get(5));
			groupRefs.put(group.refId, ref);
			groupRefs.put(group.name, ref);
		});

		// add units
		config.eachRow("units.csv", row -> {
			var ref = groupRefs.get(row.get(5));
			if (ref == null) {
				config.log().error("unknown unit group: " + row.get(5));
				return;
			}

			var unit = new Unit();
			unit.refId = row.get(0);
			unit.name = row.get(1);
			unit.description = row.get(2);
			unit.conversionFactor = row.getDouble(3);
			unit.synonyms = row.get(4);
			ref.group.units.add(unit);
			if (ref.isRefUnit(unit)) {
				ref.group.referenceUnit = unit;
			}
		});

		var groups = groupRefs.values()
				.stream()
				.map(ref -> ref.group)
				.toList();
		config.insert(groups);

	}

	private record GroupRef(UnitGroup group, String refUnit) {

		boolean isRefUnit(Unit unit) {
			return Objects.equals(refUnit, unit.refId)
					|| Objects.equals(refUnit, unit.name);
		}

	}

}
