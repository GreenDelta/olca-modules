package org.openlca.io.olca;

import java.util.Objects;

import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwSet;

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
			for (var copied : copy.nwSets) {
				for (var f : copied.factors) {
					f.impactCategory = conf.swap(f.impactCategory);
				}
			}
			for (var nwSet : method.nwSets) {
				for (var copied : copy.nwSets) {
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
		return Objects.equals(source.name, target.name)
			&& Objects.equals(source.description, target.description)
			&& Objects.equals(source.weightedScoreUnit, target.weightedScoreUnit);
	}

}
