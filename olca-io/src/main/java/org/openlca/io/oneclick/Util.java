package org.openlca.io.oneclick;

import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;

class Util {

	private Util() {
	}

	static String refQuantityOf(Process p) {
		if (p == null || p.quantitativeReference == null)
			return "error: undefined ref. quantity";
		var q = p.quantitativeReference;
		if (q.unit == null)
			return "error: undefined unit";
		return q.amount + " " + q.unit.name;
	}

	static double refMassOf(Process p) {
		if (p == null || p.quantitativeReference == null)
			return Double.NaN;
		var q = p.quantitativeReference;
		if (q.flow == null || q.unit == null || q.flowPropertyFactor == null)
			return Double.NaN;

		var qUnit = q.unit;
		var qProp = q.flowPropertyFactor;
		Unit kg = null;
		FlowPropertyFactor mass = null;
		for (var f : q.flow.flowPropertyFactors) {
			if (f.flowProperty == null
				|| f.flowProperty.unitGroup == null
				|| !"Mass".equals(f.flowProperty.name))
				continue;
			mass = f;
			for (var unit : f.flowProperty.unitGroup.units) {
				if ("kg".equals(unit.name)) {
					kg = unit;
					break;
				}
			}
		}

		if (mass == null || kg == null)
			return Double.NaN;
		return q.amount * qUnit.conversionFactor * mass.conversionFactor
			/ (kg.conversionFactor * qProp.conversionFactor);
	}

}
