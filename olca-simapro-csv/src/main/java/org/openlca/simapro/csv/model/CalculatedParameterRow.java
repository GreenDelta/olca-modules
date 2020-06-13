package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;

public class CalculatedParameterRow implements IDataRow {

	public String name;
	public String expression;
	public String comment;

	@Override
	public void fill(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		this.name = CsvUtils.get(columns, 0);
		this.expression = CsvUtils.get(columns, 1);
		this.comment = CsvUtils.readMultilines(CsvUtils.get(columns, 2));
	}

	public String toCsv(CsvConfig config) {
		return CsvUtils.getJoiner(config).join(name, expression,
				comment);
	}

	@Override
	public String toString() {
		return "CalculatedParameterRow [name=" + name + ", expression="
				+ expression + "]";
	}

}
