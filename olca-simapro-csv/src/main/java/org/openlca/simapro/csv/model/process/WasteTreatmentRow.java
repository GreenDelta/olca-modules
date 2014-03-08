package org.openlca.simapro.csv.model.process;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.AbstractExchangeRow;

public class WasteTreatmentRow extends AbstractExchangeRow {

	private String wasteType;

	private String category;

	public String getWasteType() {
		return wasteType;
	}

	public String getCategory() {
		return category;
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
		setWasteType(CsvUtils.get(columns, 3));
		setCategory(CsvUtils.get(columns, 4));
		setComment(CsvUtils.get(columns, 5));
	}
}
