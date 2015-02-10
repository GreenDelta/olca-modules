package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;

public class CalculatedParameterRow implements IDataRow {

	private String name;
	private String expression;
	private String comment;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	@Override
	public void fill(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		setName(CsvUtils.get(columns, 0));
		setExpression(CsvUtils.get(columns, 1));
		setComment(CsvUtils.readMultilines(CsvUtils.get(columns, 2)));
	}

	public String toCsv(CsvConfig config) {
		return CsvUtils.getJoiner(config).join(getName(), getExpression(),
				getComment());
	}

	@Override
	public String toString() {
		return "CalculatedParameterRow [name=" + name + ", expression="
				+ expression + "]";
	}

}
