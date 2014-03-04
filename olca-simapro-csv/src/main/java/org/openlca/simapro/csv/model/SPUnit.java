package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.CsvUtils;

public class SPUnit {

	private double conversionFactor = 1;
	private String name;
	private String referenceUnit;
	private String quantity;

	public double getConversionFactor() {
		return conversionFactor;
	}

	public String getName() {
		return name;
	}

	public void setConversionFactor(double conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getReferenceUnit() {
		return referenceUnit;
	}

	public void setReferenceUnit(String unit) {
		this.referenceUnit = unit;
	}

	public static SPUnit fromLine(String line, String separator) {
		String[] columns = CsvUtils.split(line, separator);
		SPUnit unit = new SPUnit();
		unit.name = CsvUtils.get(columns, 0);
		unit.quantity = CsvUtils.get(columns, 1);
		Double f = CsvUtils.getDouble(columns, 2);
		unit.conversionFactor = f != null ? f : 1d;
		unit.referenceUnit = CsvUtils.get(columns, 3);
		return unit;
	}

	public String toLine(String separator) {
		return CsvUtils.getJoiner(separator).join(name, quantity,
				conversionFactor, referenceUnit);
	}

}
