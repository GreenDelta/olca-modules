package org.openlca.simapro.csv.model.process;

import org.openlca.simapro.csv.model.AbstractExchangeRow;

public abstract class RefProductRow extends AbstractExchangeRow {

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

}
