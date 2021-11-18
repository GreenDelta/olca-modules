package org.openlca.io.simapro.csv.input;

import java.util.UUID;

import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.simapro.csv.refdata.CalculatedParameterRow;
import org.openlca.simapro.csv.refdata.InputParameterRow;

class Parameters {

	private Parameters() {
	}

	static Parameter create(InputParameterRow row, ParameterScope scope) {
		Parameter p = new Parameter();
		p.refId = UUID.randomUUID().toString();
		p.name = row.name();
		p.isInputParameter = true;
		p.scope = scope;
		p.value = row.value();
		p.description = row.comment();
		p.uncertainty = Uncertainties.of(row.value(), row.uncertainty());
		return p;
	}

	static Parameter create(
		ImportContext context, CalculatedParameterRow row, ParameterScope scope) {
		Parameter p = new Parameter();
		p.refId = UUID.randomUUID().toString();
		p.name = row.name();
		p.scope = scope;
		p.description = row.comment();
		p.isInputParameter = false;
		p.formula = context.convertFormula(row.expression());
		return p;
	}
}
