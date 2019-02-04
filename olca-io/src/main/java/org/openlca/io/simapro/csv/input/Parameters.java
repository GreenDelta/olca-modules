package org.openlca.io.simapro.csv.input;

import java.util.UUID;

import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.simapro.csv.model.CalculatedParameterRow;
import org.openlca.simapro.csv.model.InputParameterRow;

class Parameters {

	private Parameters() {
	}

	static Parameter create(InputParameterRow row, ParameterScope scope) {
		Parameter p = new Parameter();
		p.refId = UUID.randomUUID().toString();
		p.name = row.getName();
		p.isInputParameter = true;
		p.scope = scope;
		p.value = row.getValue();
		p.formula = Double.toString(row.getValue());
		p.description = row.getComment();
		p.uncertainty = Uncertainties.get(row.getValue(),
		row.getUncertainty());
		return p;
	}

	static Parameter create(CalculatedParameterRow row, ParameterScope scope) {
		Parameter p = new Parameter();
		p.refId = UUID.randomUUID().toString();
		p.name = row.getName();
		p.scope = scope;
		p.description = row.getComment();
		p.isInputParameter = false;
		String expr = row.getExpression();
		if (expr.contains("(") && expr.contains(",")) {
			// openLCA uses semicolons as parameter separators in functions
			// but SimaPro uses commas here; However, this will fail anyhow
			// if the decimal separator is a comma... There is currently no
			// good solution for this problem.
			expr = expr.replaceAll(",", ";");
		}
		p.formula = expr;
		return p;
	}
}
