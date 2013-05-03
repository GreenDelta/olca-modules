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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.openlca.core.model.modelprovider.IModelComponent;

@Entity
@Table(name = "tbl_flows")
public class Flow extends AbstractEntity implements IModelComponent,
		Copyable<Flow>, IdentifyableByVersionAndUUID, PropertyChangeListener {

	@Column(length = 36, name = "categoryid")
	private String categoryId;

	@Lob
	@Column(name = "description")
	private String description;

	@Column(name = "flowtype")
	private FlowType flowType;

	@Column(name = "name")
	private String name;

	@Column(name = "cas_number")
	private String casNumber;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "f_flowinformation")
	private final List<FlowPropertyFactor> flowPropertyFactors = new ArrayList<>();

	@Column(name = "formula")
	private String formula;

	@Column(name = "infrastructure_flow")
	private boolean infrastructureFlow;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_location")
	private Location location;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_reference_flow_property")
	private FlowProperty referenceFlowProperty;

	@Transient
	private transient final PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	public Flow() {
	}

	public Flow(final String id, final String name) {
		setId(id);
		this.name = name;
	}

	@Override
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	@PostLoad
	protected void postLoad() {
		for (FlowPropertyFactor factor : flowPropertyFactors) {
			factor.addPropertyChangeListener(this);
		}
		if (referenceFlowProperty != null)
			referenceFlowProperty.addPropertyChangeListener(this);
	}

	@Override
	public String getCategoryId() {
		return categoryId;
	}

	@Override
	public Flow copy() {
		Flow flow = new Flow(UUID.randomUUID().toString(), getName());
		flow.setCategoryId(getCategoryId());
		flow.setDescription(getDescription());
		flow.setFlowType(getFlowType());
		flow.setCasNumber(getCasNumber());
		flow.setFormula(getFormula());
		flow.setInfrastructureFlow(isInfrastructureFlow());
		flow.setLocation(getLocation());
		flow.setReferenceFlowProperty(getReferenceFlowProperty());
		for (FlowPropertyFactor factor : getFlowPropertyFactors()) {
			flow.add(factor.copy());
		}
		return flow;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public FlowType getFlowType() {
		return flowType;
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

	public void setFlowType(final FlowType flowType) {
		support.firePropertyChange("flowType", this.flowType,
				this.flowType = flowType);
	}

	@Override
	public void setName(final String name) {
		support.firePropertyChange("name", this.name, this.name = name);
	}

	@Override
	public String toString() {
		return "Flow [flowType=" + flowType + ", name=" + name + ", getId()="
				+ getId() + "]";
	}

	public void add(final FlowPropertyFactor flowPropertyFactor) {
		if (!flowPropertyFactors.contains(flowPropertyFactor)) {
			flowPropertyFactors.add(flowPropertyFactor);
			flowPropertyFactor.addPropertyChangeListener(this);
			support.firePropertyChange("flowPropertyFactors", null,
					flowPropertyFactor);
		}
	}

	public String getCasNumber() {
		return casNumber;
	}

	public FlowPropertyFactor getFlowPropertyFactor(String flowPropertyId) {
		for (FlowPropertyFactor factor : flowPropertyFactors) {
			if (factor.getFlowProperty() != null && flowPropertyId != null
					&& flowPropertyId.equals(factor.getFlowProperty().getId())) {
				return factor;
			}
		}
		return null;
	}

	public FlowPropertyFactor getReferencePropertyFactor() {
		return getFlowPropertyFactor(getReferenceFlowProperty().getId());
	}

	public FlowPropertyFactor[] getFlowPropertyFactors() {
		return flowPropertyFactors
				.toArray(new FlowPropertyFactor[flowPropertyFactors.size()]);
	}

	public String getFormula() {
		return formula;
	}

	public Location getLocation() {
		return location;
	}

	public FlowProperty getReferenceFlowProperty() {
		return referenceFlowProperty;
	}

	public boolean isInfrastructureFlow() {
		return infrastructureFlow;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent arg0) {
		support.firePropertyChange(arg0);
	}

	public void remove(final FlowPropertyFactor flowPropertyFactor) {
		flowPropertyFactors.remove(flowPropertyFactor);
		if (flowPropertyFactor != null) {
			flowPropertyFactor.removePropertyChangeListener(this);
		}
		support.firePropertyChange("flowPropertyFactors", flowPropertyFactor,
				null);
	}

	public void setCasNumber(final String casNumber) {
		support.firePropertyChange("casNumber", this.casNumber,
				this.casNumber = casNumber);
	}

	public void setFormula(final String formula) {
		support.firePropertyChange("formula", this.formula,
				this.formula = formula);
	}

	public void setInfrastructureFlow(final boolean isInfrastructureProcess) {
		support.firePropertyChange("infrastructureFlow",
				this.infrastructureFlow,
				this.infrastructureFlow = isInfrastructureProcess);
	}

	public void setLocation(final Location location) {
		support.firePropertyChange("location", this.location,
				this.location = location);
	}

	public void setReferenceFlowProperty(
			final FlowProperty referenceFlowProperty) {
		support.firePropertyChange("referenceFlowProperty",
				this.referenceFlowProperty,
				this.referenceFlowProperty = referenceFlowProperty);
	}

}
