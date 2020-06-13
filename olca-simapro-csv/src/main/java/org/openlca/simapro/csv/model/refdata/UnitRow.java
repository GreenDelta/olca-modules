package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.IDataRow;

public class UnitRow implements IDataRow {

	public double conversionFactor = 1;
	public String name;
	public String referenceUnit;
	public String quantity;

	@Override
	public void fill(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		name = CsvUtils.get(columns, 0);
		quantity = CsvUtils.get(columns, 1);
		Double f = CsvUtils.getDouble(columns, 2);
		conversionFactor = f != null ? f : 1d;
		referenceUnit = CsvUtils.get(columns, 3);
	}

	public String toLine(CsvConfig config) {
		return CsvUtils.getJoiner(config).join(name, quantity,
				conversionFactor, referenceUnit);
	}

	@Override
	public String toString() {
		return "UnitRow [name=" + name + "]";
	}

}
