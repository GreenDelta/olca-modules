package org.openlca.ilcd.util;

import javax.xml.namespace.QName;

import org.openlca.ilcd.units.Unit;

public class UnitExtension {

	private Unit unit;
	private final String UNIT_ID = "unitId";

	public UnitExtension(Unit unit) {
		this.unit = unit;
	}

	public boolean isValid() {
		return unit != null && getUnitId() != null;
	}

	public void setUnitId(String id) {
		if (unit == null)
			return;
		QName qName = Extensions.getQName(UNIT_ID);
		unit.otherAttributes.put(qName, id);
	}

	public String getUnitId() {
		if (unit == null)
			return null;
		QName qName = Extensions.getQName(UNIT_ID);
		return unit.otherAttributes.get(qName);
	}

}
