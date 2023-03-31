package org.openlca.io.olca;

import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.util.Strings;

class ImpactMethodImport {

	private final Config conf;

	private ImpactMethodImport(Config conf) {
		this.conf = conf;
	}

	static void run(Config config) {
		new ImpactMethodImport(config).run();
	}

	private void run() {
		conf.syncAll(ImpactMethod.class, method -> {
			var copy = method.copy();
			copy.source = conf.swap(method.source);

			// swap impact categories
			copy.impactCategories.clear();
			for (var impact : method.impactCategories) {
				var swapped = conf.swap(impact);
				if (swapped != null) {
					copy.impactCategories.add(swapped);
				}
			}

			// swap impact categories in NW-sets
			for (NwSet nwSet : method.nwSets) {
				for (NwFactor f : nwSet.factors) {
					f.impactCategory = conf.swap(f.impactCategory);
				}
				for (NwSet copied : copy.nwSets) {
					// we need to set the reference IDs from the source as they are
					// generated new in the clone method.
					if (areEqual(copied, nwSet)) {
						copied.refId = nwSet.refId;
						break;
					}
				}
			}

			return copy;
		});
	}

	private boolean areEqual(NwSet source, NwSet target) {
		return Strings.nullOrEqual(source.name, target.name)
				&& Strings.nullOrEqual(source.description, target.description)
				&& Strings.nullOrEqual(source.weightedScoreUnit, target.weightedScoreUnit);
	}

}
