package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.CsvUtils;

public class SPCalculatedParameter extends SPParameter {

	private String expression;

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public static SPCalculatedParameter fromCsv(String line, String separator) {
		String[] columns = CsvUtils.split(line, separator);
		SPCalculatedParameter param = new SPCalculatedParameter();
		param.setName(CsvUtils.get(columns, 0));
		param.setExpression(CsvUtils.get(columns, 1));
		param.setComment(CsvUtils.readMultilines(CsvUtils.get(columns, 3)));
		return param;
	}

	public String toCsv(String separator) {
		return CsvUtils.getJoiner(separator).join(getName(), getExpression(),
				getComment());
	}

}
