package org.openlca.core.database.usage;

import org.openlca.formula.Formulas;

class Formula {

	static boolean contains(String formula, String variable) {
		if (formula == null)
			return false;
		var f = formula.trim();
		if (f.equalsIgnoreCase(variable))
			return true;
		try {
			var vars = Formulas.getVariables(f);
			for (var var : vars) {
				if (var.equalsIgnoreCase(variable))
					return true;
			}
		} catch (Error e) {
			return false;
		}
		return false;
	}
}
