package org.openlca.io.ilcd.output;

import org.openlca.core.model.ImpactMethod;
import org.openlca.ilcd.methods.LCIAMethod;

public class ImpactMethodExport {

	private final Export exp;

	public ImpactMethodExport(Export exp) {
		this.exp = exp;
	}

	public void write(ImpactMethod method) {
		if (method == null)
			return;
		for (var impact : method.impactCategories) {
			if (exp.store.contains(LCIAMethod.class, impact.refId))
				continue;
			exp.write(impact);
		}
	}
}
