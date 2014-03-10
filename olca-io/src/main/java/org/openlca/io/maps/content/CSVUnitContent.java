package org.openlca.io.maps.content;

import org.openlca.io.KeyGen;
import org.openlca.simapro.csv.model.refdata.Quantity;
import org.openlca.simapro.csv.model.refdata.UnitRow;

public class CSVUnitContent implements IMappingContent {

	private String unit;
	private double conversionFactor;
	private Quantity quantity;

	public CSVUnitContent() {
	}

	public CSVUnitContent(String unit, double conversionFactor,
			Quantity quantity) {
		this.unit = unit;
		this.conversionFactor = conversionFactor;
		this.quantity = quantity;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Quantity getQuantity() {
		return quantity;
	}

	public void setQuantity(Quantity quantity) {
		this.quantity = quantity;
	}

	public UnitRow createUnit() {
		UnitRow unit = new UnitRow(this.unit);
		unit.setConversionFactor(conversionFactor);
		unit.setQuantity(quantity.getName());
		unit.setReferenceUnit(quantity.getReferenceUnit().getName());
		return unit;
	}

	@Override
	public String getKey() {
		return KeyGen.get(unit);
	}

}
