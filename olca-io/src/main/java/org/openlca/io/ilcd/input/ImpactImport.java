package org.openlca.io.ilcd.input;

import org.openlca.core.model.ImpactCategory;
import org.openlca.ilcd.methods.LCIAMethod;

public record ImpactImport(ImportConfig config, LCIAMethod dataSet) {

	static ImpactCategory get(ImportConfig config, String id) {
		var impact = config.db().get(ImpactCategory.class, id);
		if (impact != null)
			return impact;
		var dataSet = config.store().get(LCIAMethod.class, id);
		if (dataSet == null) {
			config.log().error("invalid reference in ILCD data set:" +
				" impact method '" + id + "' does not exist");
			return null;
		}
		return new ImpactImport(config, dataSet).run();
	}

	public ImpactCategory run() {

	}
}
