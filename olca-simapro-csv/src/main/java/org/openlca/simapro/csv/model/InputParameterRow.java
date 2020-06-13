package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;

public class InputParameterRow implements IDataRow {

	public String name;
	public String comment;
	public Uncertainty uncertainty;
	public double value;
	public boolean hidden;

	@Override
	public void fill(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		this.name = CsvUtils.get(columns, 0);
		Double val = CsvUtils.getDouble(columns, 1);
		this.value = val == null ? 0 : val;
		this.uncertainty = Uncertainty.fromCsv(columns, 2);
		String hiddenStr = CsvUtils.get(columns, 6);
		this.hidden = hiddenStr != null && hiddenStr.equalsIgnoreCase("yes");
		this.comment = CsvUtils.readMultilines(CsvUtils.get(columns, 7));
	}

	public String toCsv(CsvConfig config) {
		String[] line = new String[8];
		line[0] = name;
		line[1] = Double.toString(value);
		if (uncertainty != null)
			uncertainty.toCsv(line, 2);
		else
			Uncertainty.undefinedToCsv(line, 2);
		line[6] = hidden ? "Yes" : "No";
		line[7] = CsvUtils.writeMultilines(comment);
		return CsvUtils.getJoiner(config).join(line);
	}

	@Override
	public String toString() {
		return "InputParameterRow [name=" + name + ", value=" + value + "]";
	}

}
