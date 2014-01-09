package org.openlca.simapro.csv.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a SimaPro quantity
 */
public class SPQuantity {

	private boolean dimensional = true;
	private String name;
	private SPUnit referenceUnit;

	private List<SPUnit> units = new ArrayList<SPUnit>();

	public SPQuantity(String name, SPUnit referenceUnit) {
		this.name = name;
		this.referenceUnit = referenceUnit;
		this.units.add(referenceUnit);
	}

	public SPQuantity(String name, SPUnit referenceUnit, boolean dimensional) {
		this.name = name;
		this.referenceUnit = referenceUnit;
		this.dimensional = dimensional;
		this.units.add(referenceUnit);
	}

	public void add(SPUnit unit) {
		this.units.add(unit);
	}

	public String getName() {
		return name;
	}

	public SPUnit getReferenceUnit() {
		return referenceUnit;
	}

	public SPUnit[] getUnits() {
		return units.toArray(new SPUnit[units.size()]);
	}

	public boolean isDimensional() {
		return dimensional;
	}

	public void setDimensional(boolean dimensional) {
		this.dimensional = dimensional;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setReferenceUnit(SPUnit referenceUnit) {
		this.referenceUnit = referenceUnit;
	}

}
