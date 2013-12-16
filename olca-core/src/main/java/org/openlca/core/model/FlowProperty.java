package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Flow properties are quantities like mass, volume, etc.
 */
@Entity
@Table(name = "tbl_flow_properties")
public class FlowProperty extends CategorizedEntity {

	@Column(name = "flow_property_type")
	private FlowPropertyType flowPropertyType;

	@OneToOne
	@JoinColumn(name = "f_unit_group")
	private UnitGroup unitGroup;

	@Override
	public FlowProperty clone() {
		FlowProperty flowProperty = new FlowProperty();
		flowProperty.setName(getName());
		flowProperty.setCategory(getCategory());
		flowProperty.setDescription(getDescription());
		flowProperty.setUnitGroup(getUnitGroup());
		flowProperty.setFlowPropertyType(getFlowPropertyType());
		return flowProperty;
	}

	public FlowPropertyType getFlowPropertyType() {
		return flowPropertyType;
	}

	public void setFlowPropertyType(FlowPropertyType flowPropertyType) {
		this.flowPropertyType = flowPropertyType;
	}

	public UnitGroup getUnitGroup() {
		return unitGroup;
	}

	public void setUnitGroup(UnitGroup unitGroup) {
		this.unitGroup = unitGroup;
	}

}
