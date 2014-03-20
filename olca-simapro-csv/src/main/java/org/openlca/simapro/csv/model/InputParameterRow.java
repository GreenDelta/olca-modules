package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;

public class InputParameterRow implements IDataRow {

	private String name;
	private String comment;
	private Uncertainty uncertainty;
	private double value;
	private boolean hidden;

	public Uncertainty getUncertainty() {
		return uncertainty;
	}

	public void setUncertainty(Uncertainty distribution) {
		this.uncertainty = distribution;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public String getName() {
		return name;
	}

	public String getComment() {
		return comment;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public void fill(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		setName(CsvUtils.get(columns, 0));
		Double val = CsvUtils.getDouble(columns, 1);
		setValue(val == null ? 0 : val);
		setUncertainty(Uncertainty.fromCsv(columns, 2));
		String hiddenStr = CsvUtils.get(columns, 6);
		setHidden(hiddenStr != null && hiddenStr.equalsIgnoreCase("yes"));
		setComment(CsvUtils.readMultilines(CsvUtils.get(columns, 7)));
	}

	public String toCsv(CsvConfig config) {
		String[] line = new String[8];
		line[0] = getName();
		line[1] = Double.toString(value);
		if (uncertainty != null)
			uncertainty.toCsv(line, 2);
		else
			Uncertainty.undefinedToCsv(line, 2);
		line[6] = hidden ? "Yes" : "No";
		line[7] = CsvUtils.writeMultilines(getComment());
		return CsvUtils.getJoiner(config).join(line);
	}

	@Override
	public String toString() {
		return "InputParameterRow [name=" + name + ", value=" + value + "]";
	}

}
