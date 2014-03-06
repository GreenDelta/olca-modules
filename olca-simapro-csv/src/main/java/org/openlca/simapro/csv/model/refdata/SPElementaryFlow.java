package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.IDataRow;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

public abstract class SPElementaryFlow implements IDataRow {

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

	public abstract ElementaryFlowType getFlowType();

	@Override
	public void fill(String line, String separator) {
		String[] columns = CsvUtils.split(line, separator);
		name = CsvUtils.get(columns, 0);
		referenceUnit = CsvUtils.get(columns, 1);
		casNumber = CsvUtils.get(columns, 2);
		comment = CsvUtils.get(columns, 3);
	}

}
