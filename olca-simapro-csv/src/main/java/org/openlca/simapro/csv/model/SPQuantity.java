package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.annotations.BlockRow;

@BlockRow("Quantities")
public class SPQuantity implements IDataRow {

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

	public String toLine(String separator) {
		return CsvUtils.getJoiner(separator).join(name,
				withDimension ? "Yes" : "No");
	}

	@Override
	public void fill(String line, String separator) {
		String[] columns = CsvUtils.split(line, separator);
		name = CsvUtils.get(columns, 0);
		String dimStr = CsvUtils.get(columns, 1);
		if (dimStr != null)
			withDimension = dimStr.equalsIgnoreCase("yes");
	}
}
