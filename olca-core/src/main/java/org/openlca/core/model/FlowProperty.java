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

	@Override
	public FlowProperty clone() {
		FlowProperty clone = new FlowProperty();
		Util.copyRootFields(this, clone);
		clone.category = category;
		clone.unitGroup = unitGroup;
		clone.flowPropertyType = flowPropertyType;
		return clone;
	}

}
