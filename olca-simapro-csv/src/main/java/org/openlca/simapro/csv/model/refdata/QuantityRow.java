package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.IDataRow;

public class QuantityRow implements IDataRow {

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

	public String toLine(CsvConfig config) {
		return CsvUtils.getJoiner(config).join(name,
				withDimension ? "Yes" : "No");
	}

	@Override
	public void fill(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		name = CsvUtils.get(columns, 0);
		String dimStr = CsvUtils.get(columns, 1);
		if (dimStr != null)
			withDimension = dimStr.equalsIgnoreCase("yes");
	}

	@Override
	public String toString() {
		return "Quantity [name=" + name + "]";
	}

}
