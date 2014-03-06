package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;

public class CalculatedParameterRow extends SPParameter {

	private String expression;

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public static CalculatedParameterRow fromCsv(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		CalculatedParameterRow param = new CalculatedParameterRow();
		param.setName(CsvUtils.get(columns, 0));
		param.setExpression(CsvUtils.get(columns, 1));
		param.setComment(CsvUtils.readMultilines(CsvUtils.get(columns, 3)));
		return param;
	}

	public String toCsv(CsvConfig config) {
		return CsvUtils.getJoiner(config).join(getName(), getExpression(),
				getComment());
	}

}
