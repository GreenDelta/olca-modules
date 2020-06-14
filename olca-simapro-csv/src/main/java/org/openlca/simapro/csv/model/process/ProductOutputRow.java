package org.openlca.simapro.csv.model.process;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;

public class ProductOutputRow extends RefProductRow {

	public String allocation;

	@Override
	public void fill(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		this.name = CsvUtils.get(columns, 0);
		this.unit = CsvUtils.get(columns, 1);
		this.amount = CsvUtils.formatNumber(CsvUtils.get(columns, 2));
		this.allocation = CsvUtils.get(columns, 3);
		this.wasteType = CsvUtils.get(columns, 4);
		this.category = CsvUtils.get(columns, 5);
		this.comment = CsvUtils.get(columns, 6);
	}

	@Override
	public String toString() {
		return "ProductOutputRow [getName()=" + name
				+ ", getUnit()=" + unit + "]";
	}

}
