package org.openlca.io.maps.content;

import org.openlca.ilcd.commons.ClassificationInformation;

public class ILCDUnitContent implements IMappingContent {

	private String unit;

	public ILCDUnitContent() {

	}

	public ILCDUnitContent(String unit) {
		this.unit = unit;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return null;
	}

}
