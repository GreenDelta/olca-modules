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
	public FlowPropertyType flowPropertyType;

	@OneToOne
	@JoinColumn(name = "f_unit_group")
	public UnitGroup unitGroup;

	public static FlowProperty of(String name, UnitGroup unitGroup) {
		var prop = new FlowProperty();
		Entities.init(prop, name);
		prop.flowPropertyType = FlowPropertyType.PHYSICAL;
		prop.unitGroup = unitGroup;
		return prop;
	}

	public Unit getReferenceUnit() {
		return unitGroup == null
				? null
				: unitGroup.referenceUnit;
	}

	@Override
	public FlowProperty clone() {
		var clone = new FlowProperty();
		Entities.copyFields(this, clone);
		clone.unitGroup = unitGroup;
		clone.flowPropertyType = flowPropertyType;
		return clone;
	}

}
