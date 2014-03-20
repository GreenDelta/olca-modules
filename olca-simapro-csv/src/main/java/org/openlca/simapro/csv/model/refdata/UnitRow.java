package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.IDataRow;

public class UnitRow implements IDataRow {

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
