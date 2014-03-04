package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.CsvUtils;

public class SPQuantity {

	private boolean withDimension = true;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isWithDimension() {
		return withDimension;
	}

	public void setWithDimension(boolean withDimension) {
		this.withDimension = withDimension;
	}

	public static SPQuantity fromLine(String line, String separator) {
		String[] columns = CsvUtils.split(line, separator);
		SPQuantity quantity = new SPQuantity();
		quantity.name = CsvUtils.get(columns, 0);
		String dimStr = CsvUtils.get(columns, 1);
		if (dimStr != null)
			quantity.withDimension = dimStr.equalsIgnoreCase("yes");
		return quantity;
	}

	public String toLine(String separator) {
		return CsvUtils.getJoiner(separator).join(name,
				withDimension ? "Yes" : "No");
	}

}
