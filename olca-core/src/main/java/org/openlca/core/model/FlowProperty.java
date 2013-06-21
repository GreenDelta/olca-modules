/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
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
public class FlowProperty extends RootEntity {

	@Column(name = "flow_property_type")
	private FlowPropertyType flowPropertyType;

	@OneToOne
	@JoinColumn(name = "f_unit_group")
	private UnitGroup unitGroup;

	@Override
	public FlowProperty clone() {
		FlowProperty flowProperty = new FlowProperty();
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
