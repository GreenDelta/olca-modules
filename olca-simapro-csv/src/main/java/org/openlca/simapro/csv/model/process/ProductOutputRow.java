package org.openlca.simapro.csv.model.process;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;

public class ProductOutputRow extends RefProductRow {

	public double allocation = 100;

	@Override
	public void fill(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		this.name = CsvUtils.get(columns, 0);
		this.unit = CsvUtils.get(columns, 1);
		this.amount = CsvUtils.formatNumber(CsvUtils.get(columns, 2));
		Double allocation = CsvUtils.getDouble(columns, 3);
		this.allocation = allocation == null ? 100 : allocation;
		this.wasteType = CsvUtils.get(columns, 4);
		this.category = CsvUtils.get(columns, 5);
		this.comment = CsvUtils.get(columns, 6);
	}

	@Override
	public String toString() {
		return "ProductOutputRow [getName()=" + name + ", getUnit()="
				+ unit + "]";
	}

}
