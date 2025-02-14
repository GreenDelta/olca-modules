package org.openlca.io.ilcd.output;

import org.openlca.core.model.ImpactMethod;

public class ImpactMethodExport {

	private final Export exp;
	private final ImpactMethod method;

	public ImpactMethodExport(Export exp, ImpactMethod method) {
		this.exp = exp;
		this.method = method;
	}

	public void write() {
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
