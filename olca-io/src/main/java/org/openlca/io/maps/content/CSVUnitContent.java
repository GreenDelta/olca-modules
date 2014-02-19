package org.openlca.io.maps.content;

import org.openlca.io.KeyGen;
import org.openlca.simapro.csv.model.SPQuantity;
import org.openlca.simapro.csv.model.SPUnit;

public class CSVUnitContent implements IMappingContent {

	private String unit;
	private double conversionFactor;
	private SPQuantity quantity;

	public CSVUnitContent() {
	}

	public CSVUnitContent(String unit, double conversionFactor,
			SPQuantity quantity) {
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

	public SPQuantity getQuantity() {
		return quantity;
	}

	public void setQuantity(SPQuantity quantity) {
		this.quantity = quantity;
	}

	public SPUnit createUnit() {
		SPUnit unit = new SPUnit(this.unit);
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
