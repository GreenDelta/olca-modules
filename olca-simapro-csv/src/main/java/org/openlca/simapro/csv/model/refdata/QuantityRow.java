package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.IDataRow;

public class QuantityRow implements IDataRow {

	public String name;
	public boolean withDimension = true;

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
