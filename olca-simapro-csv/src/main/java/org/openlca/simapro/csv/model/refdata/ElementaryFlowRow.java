package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.IDataRow;

public class ElementaryFlowRow implements IDataRow {

	private String name;
	private String referenceUnit;
	private String casNumber;
	private String comment;

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCASNumber() {
		return casNumber;
	}

	public String getName() {
		return name;
	}

	public String getReferenceUnit() {
		return referenceUnit;
	}

	public void setCASNumber(String casNumber) {
		this.casNumber = casNumber;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setReferenceUnit(String referenceUnit) {
		this.referenceUnit = referenceUnit;
	}

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
