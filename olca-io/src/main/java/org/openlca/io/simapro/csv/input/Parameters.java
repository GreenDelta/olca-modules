package org.openlca.io.simapro.csv.input;

import java.util.UUID;

import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.FormulaConverter;
import org.openlca.simapro.csv.refdata.CalculatedParameterRow;
import org.openlca.simapro.csv.refdata.InputParameterRow;
import org.openlca.util.Strings;

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
		CsvDataSet dataSet, CalculatedParameterRow row, ParameterScope scope) {
		Parameter p = new Parameter();
		p.refId = UUID.randomUUID().toString();
		p.name = row.name();
		p.scope = scope;
		p.description = row.comment();
		p.isInputParameter = false;
		p.formula = formulaOf(dataSet, row.expression());
		return p;
	}

	/**
	 * Converts a formula into syntactic form that can be understood by the
	 * openLCA formula interpreter.
	 */
	static String formulaOf(CsvDataSet dataSet, String expression) {
		if (Strings.nullOrEmpty(expression))
			return null;
		return FormulaConverter.of(dataSet.header())
			.decimalSeparator('.')
			.parameterSeparator(';')
			.convert(expression);
	}
}
