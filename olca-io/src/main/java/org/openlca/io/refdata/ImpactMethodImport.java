package org.openlca.io.refdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;

class ImpactMethodImport implements Runnable {

	private final ImportConfig config;

	ImpactMethodImport(ImportConfig config) {
		this.config = config;
	}

	@Override
	public void run() {
		var map = new HashMap<String, ImpactMethod>();

		// collect meta-data of LCIA categories
		config.eachRowOf("lcia_methods.csv", row -> {
			var method = new ImpactMethod();
			method.refId = row.get(0);
			method.name = row.get(1);
			method.description = row.get(2);
			method.category = config.category(ModelType.IMPACT_METHOD, row.get(3));
			map.put(method.refId, method);
			map.put(method.name, method);
		});

		// link LCIA methods with LCIA categories
		config.eachRowOf("lcia_method_categories.csv", row -> {
			var method = map.get(row.get(0));
			if (method == null) {
				config.log().error("unknown LCIA method: " + row.get(0));
				return;
			}
			var impact = config.get(ImpactCategory.class, row.get(1));
			if (impact == null) {
				config.log().error("unknown LCIA category: " + row.get(1));
				return;
			}
			method.impactCategories.add(impact);
		});

		// add NW sets
		config.eachRowOf("lcia_method_nw_sets.csv", row -> {
			var method = map.get(row.get(0));
			if (method == null) {
				config.log().error("unknown LCIA method: " + row.get(0));
				return;
			}
			var impact = config.get(ImpactCategory.class, row.get(3));
			if (impact == null) {
				config.log().error("unknown LCIA category: " + row.get(1));
				return;
			}
			var nwSet = nwSetOf(method, row);
			var factor = new NwFactor();
			factor.normalisationFactor = row.getOptionalDouble(4);
			factor.weightingFactor = row.getOptionalDouble(5);
			nwSet.factors.add(factor);
		});

		var methods = new ArrayList<>(map.values());
		config.insert(methods);
	}

	private NwSet nwSetOf(ImpactMethod method, CsvRow row) {
		var refId = row.get(1);
		var name = row.get(2);
			for (var nwSet : method.nwSets) {
				if (Objects.equals(refId, nwSet.refId)
					|| Objects.equals(name, nwSet.name))
					return nwSet;
			}
			var nwSet = new NwSet();
			nwSet.refId = refId;
			nwSet.name = name;
			nwSet.weightedScoreUnit = row.get(6);
			method.nwSets.add(nwSet);
			return nwSet;
	}
}
