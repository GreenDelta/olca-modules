package org.openlca.io.refdata;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.commons.Strings;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

class ImpactCategoryImport implements Runnable {

	private final ImportConfig config;

	ImpactCategoryImport(ImportConfig config) {
		this.config = config;
	}

	@Override
	public void run() {

		var map = new HashMap<String, ImpactCategory>();

		// collect meta-data of LCIA categories
		config.eachRowOf("lcia_categories.csv", row -> {
			var impact = new ImpactCategory();
			impact.refId = row.get(0);
			impact.name = row.get(1);
			impact.description = row.get(2);
			impact.category = config.category(ModelType.IMPACT_CATEGORY, row.get(3));
			impact.referenceUnit = row.get(4);
			map.put(impact.refId, impact);
		});

		// map LCIA factors
		var fs = new File(config.dir(), "lcia_factors").listFiles();
		List<File> files = fs != null
				? Arrays.asList(fs)
				: List.of();
		for (var file : files) {
			config.eachRowOf(file, row -> {
				var impact = map.get(row.get(0));
				if (impact == null) {
					config.log().error("unknown impact category: " + row.get(0));
					return;
				}
				var flow = config.get(Flow.class, row.get(1));
				if (flow == null) {
					config.log().error("unknown flow: " + row.get(1));
					return;
				}
				var factor = impact.factor(flow, 1);
				mapUnit(factor, row);
				mapValue(factor, row.get(5));
				mapLocation(factor, row.get(4));
			});
		}

		var impacts = new ArrayList<>(map.values());
		config.insert(impacts);
	}

	private void mapValue(ImpactFactor factor, String val) {
		if (Strings.isBlank(val)) {
			factor.value = 0;
			return;
		}
		try {
			factor.value = Double.parseDouble(val);
		} catch (Exception ignored) {
			factor.formula = val;
		}
	}

	private void mapLocation(ImpactFactor factor, String ref) {
		if (Strings.isBlank(ref))
			return;
		var location = config.get(Location.class, ref);
		if (location == null) {
			config.log().error("unknown location: " + ref);
			return;
		}
		factor.location = location;
	}

	private void mapUnit(ImpactFactor factor, CsvRow row) {
		var property = config.get(FlowProperty.class, row.get(2));
		if (property == null) {
			config.log().error("unknown flow property: " + row.get(2));
			return;
		}

		var propFac = factor.flow.getFactor(property);
		if (propFac == null || property.unitGroup == null) {
			config.log().error("flow property '" + row.get(2)
					+ "' is not a valid flow property of flow "
					+ factor.flow.refId);
			return;
		}

		var unitRef = row.get(3);
		Unit unit = null;
		for (var u : property.unitGroup.units) {
			if (Objects.equals(unitRef, u.refId)
					|| Objects.equals(unitRef, u.name)) {
				unit = u;
				break;
			}
		}

		if (unit == null) {
			config.log().error("unit '" + unitRef
					+ "' is not a valid unit of flow "
					+ factor.flow.refId);
			return;
		}

		factor.flowPropertyFactor = propFac;
		factor.unit = unit;
	}
}
