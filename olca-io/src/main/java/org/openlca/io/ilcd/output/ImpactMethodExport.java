package org.openlca.io.ilcd.output;

import org.openlca.core.model.ImpactMethod;

public class ImpactMethodExport {

	private final Export exp;

	public ImpactMethodExport(Export exp) {
		this.exp = exp;
	}

	public void write(ImpactMethod method) {
		if (method == null)
			return;
		for (var impact : method.impactCategories) {
			if (exp.store.contains(
					org.openlca.ilcd.methods.ImpactMethod.class, impact.refId))
				continue;
			exp.write(impact);
		}
	}
}
