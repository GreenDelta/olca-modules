package org.openlca.simapro.csv.model.process;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;

public class ProductOutputRow extends RefProductRow {

	private double allocation = 100;

	public double getAllocation() {
		return allocation;
	}

	public void setAllocation(double allocation) {
		this.allocation = allocation;
	}

	@Override
	public void fill(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		setName(CsvUtils.get(columns, 0));
		setUnit(CsvUtils.get(columns, 1));
		setAmount(CsvUtils.formatNumber(CsvUtils.get(columns, 2)));
		Double allocation = CsvUtils.getDouble(columns, 3);
		setAllocation(allocation == null ? 100 : allocation);
		setWasteType(CsvUtils.get(columns, 4));
		setCategory(CsvUtils.get(columns, 5));
		setComment(CsvUtils.get(columns, 6));
	}

	@Override
	public String toString() {
		return "ProductOutputRow [getName()=" + getName() + ", getUnit()="
				+ getUnit() + "]";
	}

}
