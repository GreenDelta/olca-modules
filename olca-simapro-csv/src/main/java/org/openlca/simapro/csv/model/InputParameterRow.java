package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;

public class InputParameterRow extends SPParameter {

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

	public static InputParameterRow fromCsv(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		InputParameterRow param = new InputParameterRow();
		param.setName(CsvUtils.get(columns, 0));
		Double val = CsvUtils.getDouble(columns, 1);
		param.setValue(val == null ? 0 : val);
		param.setDistribution(SPUncertainty.fromCsv(columns, 2));
		String hiddenStr = CsvUtils.get(columns, 6);
		param.setHidden(hiddenStr != null && hiddenStr.equalsIgnoreCase("yes"));
		param.setComment(CsvUtils.readMultilines(CsvUtils.get(columns, 7)));
		return param;
	}

	public String toCsv(CsvConfig config) {
		String[] line = new String[8];
		line[0] = getName();
		line[1] = Double.toString(value);
		if (distribution != null)
			distribution.toCsv(line, 2);
		else
			SPUncertainty.undefinedToCsv(line, 2);
		line[6] = hidden ? "Yes" : "No";
		line[7] = CsvUtils.writeMultilines(getComment());
		return CsvUtils.getJoiner(config).join(line);
	}

}
