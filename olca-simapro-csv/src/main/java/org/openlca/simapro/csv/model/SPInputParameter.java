package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.CsvUtils;

public class SPInputParameter extends SPParameter {

	private SPUncertainty distribution;
	private double value;
	private boolean hidden;

	public SPUncertainty getDistribution() {
		return distribution;
	}

	public void setDistribution(SPUncertainty distribution) {
		this.distribution = distribution;
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

	public static SPInputParameter fromCsv(String line, String separator) {
		String[] columns = CsvUtils.split(line, separator);
		SPInputParameter param = new SPInputParameter();
		param.setName(CsvUtils.get(columns, 0));
		Double val = CsvUtils.getDouble(columns, 1);
		param.setValue(val == null ? 0 : val);
		param.setDistribution(SPUncertainty.fromCsv(columns, 2));
		String hiddenStr = CsvUtils.get(columns, 6);
		param.setHidden(hiddenStr != null && hiddenStr.equalsIgnoreCase("yes"));
		param.setComment(CsvUtils.readMultilines(CsvUtils.get(columns, 7)));
		return param;
	}

	public String toCsv(String separator) {
		String[] line = new String[8];
		line[0] = getName();
		line[1] = Double.toString(value);
		if (distribution != null)
			distribution.toCsv(line, 2);
		else
			SPUncertainty.undefinedToCsv(line, 2);
		line[6] = hidden ? "Yes" : "No";
		line[7] = CsvUtils.writeMultilines(getComment());
		return CsvUtils.getJoiner(separator).join(line);
	}

}
