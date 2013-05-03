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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.openlca.core.model.modelprovider.IModelComponent;

/**
 * <p style="margin-top: 0">
 * Defines a property of a flow. It has a unit group which declares what units
 * this flow property is representing
 * </p>
 */
@Entity
@Table(name = "tbl_flowproperties")
public class FlowProperty extends AbstractEntity implements
		Copyable<FlowProperty>, IModelComponent, IdentifyableByVersionAndUUID {

	@Column(length = 36, name = "categoryid")
	private String categoryId;

	@Lob
	@Column(name = "description")
	private String description;

	@Column(name = "flowpropertytype")
	private FlowPropertyType flowPropertyType;

	@Column(name = "name")
	private String name;

	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	@Column(length = 36, name = "unitgroupid")
	private String unitGroupId;

	/**
	 * <p style="margin-top: 0">
	 * Creates a new flow property
	 * </p>
	 */
	public FlowProperty() {
	}

	/**
	 * <p style="margin-top: 0">
	 * Creates a new flow property with the given unique identifier and name
	 * </p>
	 * 
	 * @param id
	 *            <p style="margin-top: 0">
	 *            The unique identifier of the flow property
	 *            </p>
	 * @param name
	 *            <p style="margin-top: 0">
	 *            The name of the flow property
	 *            </p>
	 */
	public FlowProperty(final String id, final String name) {
		setId(id);
		this.name = name;
	}

	@Override
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	@Override
	public String getCategoryId() {
		return categoryId;
	}

	@Override
	public FlowProperty copy() {
		final FlowProperty flowProperty = new FlowProperty(UUID.randomUUID()
				.toString(), getName());
		flowProperty.setCategoryId(getCategoryId());
		flowProperty.setDescription(getDescription());
		flowProperty.setUnitGroupId(getUnitGroupId());
		flowProperty.setFlowPropertyType(getFlowPropertyType());
		return flowProperty;
	}

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Getter of the flowPropertyType-field
	 * 
	 * @return The type of the flow property
	 */
	public FlowPropertyType getFlowPropertyType() {
		return flowPropertyType;
	}

	@Override
	public String getUUID() {
		return getId();
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the unitGroupId-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The id of the unit group of the flow property
	 *         </p>
	 */
	public String getUnitGroupId() {
		return unitGroupId;
	}

	@Override
	public void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	@Override
	public void setCategoryId(final String categoryId) {
		support.firePropertyChange("categoryId", this.categoryId,
				this.categoryId = categoryId);
	}

	@Override
	public void setDescription(final String description) {
		support.firePropertyChange("description", this.description,
				this.description = description);
	}

	/**
	 * Setter of the flowPropertyType-field
	 * 
	 * @param flowPropertyType
	 *            The new type of the flow property
	 */
	public void setFlowPropertyType(final FlowPropertyType flowPropertyType) {
		this.flowPropertyType = flowPropertyType;
	}

	@Override
	public void setName(final String name) {
		support.firePropertyChange("name", this.name, this.name = name);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the unitGroupId-field
	 * </p>
	 * 
	 * @param unitGroupId
	 *            <p style="margin-top: 0">
	 *            The id of the unit group of the flow property
	 *            </p>
	 */
	public void setUnitGroupId(final String unitGroupId) {
		support.firePropertyChange("unitGroupId", this.unitGroupId,
				this.unitGroupId = unitGroupId);
	}

}
