package org.openlca.simapro.csv.model.process;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.IDataRow;
import org.openlca.simapro.csv.model.SPExchange;

public class ProductOutputRow extends SPExchange implements IDataRow {

	private double allocation = 100;
	private String wasteType;
	private String category;

	public double getAllocation() {
		return allocation;
	}

	public String getWasteType() {
		return wasteType;
	}

	public String getCategory() {
		return category;
	}

	public void setAllocation(double allocation) {
		this.allocation = allocation;
	}

	public void setWasteType(String wasteType) {
		this.wasteType = wasteType;
	}

	public void setCategory(String category) {
		this.category = category;
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
}
