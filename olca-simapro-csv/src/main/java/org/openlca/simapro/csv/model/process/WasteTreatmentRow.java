package org.openlca.simapro.csv.model.process;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;

public class WasteTreatmentRow extends RefProductRow {

	@Override
	public void fill(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		setName(CsvUtils.get(columns, 0));
		setUnit(CsvUtils.get(columns, 1));
		setAmount(CsvUtils.formatNumber(CsvUtils.get(columns, 2)));
		setWasteType(CsvUtils.get(columns, 3));
		setCategory(CsvUtils.get(columns, 4));
		setComment(CsvUtils.get(columns, 5));
	}

	@Override
	public String toString() {
		return "WasteTreatmentRow [getName()=" + getName() + ", getUnit()="
				+ getUnit() + "]";
	}

}
