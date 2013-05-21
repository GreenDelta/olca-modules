package org.openlca.io;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class UnitMappingEntry {

	private String unitName;
	private Unit unit;
	private UnitGroup unitGroup;
	private FlowProperty flowProperty;
	private double factor;

	public boolean isValid() {
		return unit != null && unitGroup != null && flowProperty != null;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public UnitGroup getUnitGroup() {
		return unitGroup;
	}

	public void setUnitGroup(UnitGroup unitGroup) {
		this.unitGroup = unitGroup;
	}

	public FlowProperty getFlowProperty() {
		return flowProperty;
	}

	public void setFlowProperty(FlowProperty flowProperty) {
		this.flowProperty = flowProperty;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

}
