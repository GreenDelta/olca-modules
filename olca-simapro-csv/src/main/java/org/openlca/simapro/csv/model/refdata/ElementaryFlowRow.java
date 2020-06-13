package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.IDataRow;

public class ElementaryFlowRow implements IDataRow {

	public String name;
	public String referenceUnit;
	public String casNumber;
	public String comment;

	@Override
	public void fill(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		name = CsvUtils.get(columns, 0);
		referenceUnit = CsvUtils.get(columns, 1);
		casNumber = CsvUtils.get(columns, 2);
		comment = CsvUtils.get(columns, 3);
	}

	@Override
	public String toString() {
		return "ElementaryFlowRow [name=" + name + ", referenceUnit="
				+ referenceUnit + "]";
	}

}
