package org.openlca.io.refdata;

import java.util.ArrayList;

import org.openlca.core.model.ImpactMethod;

class ImpactMethodExport implements Runnable {

	private final ExportConfig config;

	ImpactMethodExport(ExportConfig config) {
		this.config = config;
	}

	@Override
	public void run() {
		var methods = config.db().getAll(ImpactMethod.class);
		if (methods.isEmpty())
			return;
		config.sort(methods);
		var buffer = new ArrayList<>(7);

		config.writeTo("lcia_methods.csv", csv -> {
			for (var method : methods) {
				buffer.add(method.refId);
				buffer.add(method.name);
				buffer.add(method.description);
				buffer.add(config.toPath(method.category));
				csv.printRecord(buffer);
				buffer.clear();
			}
		});

		config.writeTo("lcia_method_categories.csv", csv -> {
			for (var method : methods) {
				config.sort(method.impactCategories);
				for (var indicator : method.impactCategories) {
					buffer.add(method.name);
					buffer.add(indicator.refId);
					csv.printRecord(buffer);
					buffer.clear();
				}
			}
		});

		config.writeTo("lcia_method_nw_sets.csv", csv -> {
			for (var method : methods) {
				config.sort(method.nwSets);
				for (var nwSet : method.nwSets) {
					for (var factor : nwSet.factors) {
						buffer.add(method.name);
						buffer.add(nwSet.refId);
						buffer.add(nwSet.name);

						var indicator = factor.impactCategory != null
								? factor.impactCategory.refId
								: "";

						buffer.add(indicator);
						buffer.add(factor.normalisationFactor);
						buffer.add(factor.weightingFactor);
						buffer.add(nwSet.weightedScoreUnit);
						csv.printRecord(buffer);
						buffer.clear();
					}
				}
			}
		});

	}

}
