/*******************************************************************************
 * Copyright (c) 2007 - 2013 GreenDeltaTC. All rights reserved. This program and
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

import org.openlca.core.model.modelprovider.IParameterizedComponent;
import org.openlca.util.Strings;

/**
 * <p style="margin-top: 0">
 * A process contains a set of incoming and outgoing flows
 * </p>
 */
@Entity
@Table(name = "tbl_processes")
public class Process extends AbstractEntity implements PropertyChangeListener,
		IParameterizedComponent, Copyable<Process>,
		IdentifyableByVersionAndUUID {

	@Column(name = "allocationmethod")
	private AllocationMethod allocationMethod;

	@Column(length = 36, name = "categoryid")
	private String categoryId;

	@Lob
	@Column(name = "description")
	private String description;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "f_owner")
	private List<Exchange> exchanges = new ArrayList<>();

	@Lob
	@Column(name = "geographycomment")
	private String geographyComment;

	@Column(name = "infrastructureprocess")
	private boolean infrastructureProcess;

	@OneToOne
	@JoinColumn(name = "f_location")
	private Location location;

	@Column(name = "name")
	private String name;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "f_owner")
	private final List<Parameter> parameters = new ArrayList<>();

	// cannot rely on JPA for adminInfo and modelingAndValidation.
	// @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch =
	// FetchType.LAZY)
	// @JoinColumn(insertable = false, updatable = false, name = "id")
	@Transient
	private AdminInfo adminInfo;

	// @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch =
	// FetchType.LAZY)
	// @JoinColumn(insertable = false, updatable = false, name = "id")
	@Transient
	private ModelingAndValidation modelingAndValidation;

	@Column(name = "processtype")
	private ProcessType processType = ProcessType.UnitProcess;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "f_quantitativereference")
	private Exchange quantitativeReference;

	@Transient
	private transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	public Process() {
	}

	public Process(String id, String name) {
		setId(id);
		this.name = name;
	}

	/**
	 * Initializes the property change listener after object is loaded from
	 * database
	 */
	@PostLoad
	protected void postLoad() {
		for (Exchange exchange : getExchanges()) {
			exchange.addPropertyChangeListener(this);
		}
		for (Parameter parameter : getParameters()) {
			parameter.addPropertyChangeListener(this);
		}
	}

	/**
	 * <p style="margin-top: 0">
	 * Adds an exchange to the process
	 * 
	 * @param exchange
	 *            The exchange to be added
	 *            </p>
	 */
	public void add(Exchange exchange) {
		if (exchange != null) {
			exchanges.add(exchange);
			exchange.setOwnerId(getId());
			exchange.addPropertyChangeListener(this);
		}
		support.firePropertyChange("exchanges", null, exchange);
	}

	@Override
	public void add(Parameter parameter) {
		if (parameter != null) {
			parameters.add(parameter);
			parameter.addPropertyChangeListener(this);
			support.firePropertyChange("parameters", null, parameter);
		}
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	/**
	 * Removes all allocation factors from all exchanges
	 */
	public void clearAllocationFactors() {
		for (Exchange exchange : exchanges) {
			AllocationFactor[] factors = exchange.getAllocationFactors();
			for (AllocationFactor factor : factors) {
				exchange.remove(factor);
			}
		}
	}

	/**
	 * Checks the exchanges of the process for the given flow id
	 * 
	 * @param flowId
	 *            The id of the flow to check
	 * @param input
	 *            Indicates if the exchange is an input or not
	 * @return true if an exchange with flow with the given id is found
	 */
	public boolean contains(String flowId, boolean input) {
		boolean contains = false;
		int i = 0;
		Exchange[] exchanges = input ? getInputs() : getOutputs();
		while (i < exchanges.length && !contains) {
			contains = exchanges[i].getFlow().getId().equals(flowId);
			i++;
		}
		return contains;
	}

	/**
	 * Converts all exchanges to their reference flow property and reference
	 * unit
	 */
	public void convertExchanges() {
		for (Exchange exchange : getExchanges()) {
			exchange.getResultingAmount().setValue(
					exchange.getConvertedResult());
			exchange.getResultingAmount().setFormula(
					Double.toString(exchange.getConvertedResult()));
		}
	}

	/**
	 * Calculates the allocation factor of the given exchange for the given
	 * product
	 * 
	 * @param exchange
	 *            The exchange the allocation factor is requested for
	 * @param productId
	 *            The id of the product the allocation factor is requested for
	 * @return The allocation factor of the exchange for the product
	 */
	public double getAllocationFactor(Exchange exchange, String productId) {
		double allocationFactor = 1;
		// if no allocation is applied, the factor is 1
		if (getAllocationMethod() != null
				&& getAllocationMethod() != AllocationMethod.None) {
			// if the exchange is the product it's allocation factor is 1
			if (!exchange.getId().equals(productId)) {
				AllocationFactor productFactor = null;
				int i = 0;
				while (productFactor == null
						&& i < exchange.getAllocationFactors().length) {
					if (exchange.getAllocationFactors()[i].getProductId()
							.equals(productId)) {
						productFactor = exchange.getAllocationFactors()[i];
					}
					i++;
				}
				if (productFactor != null) {
					allocationFactor = productFactor.getValue();
				}
			}
		}
		return allocationFactor;
	}

	/**
	 * This property isn't JPA-managed.
	 * 
	 * @return
	 */
	public AdminInfo getAdminInfo() {
		return adminInfo;
	}

	public void setAdminInfo(AdminInfo adminInfo) {
		this.adminInfo = adminInfo;
	}

	/**
	 * This property isn't JPA-managed.
	 * 
	 * @return
	 */
	public ModelingAndValidation getModelingAndValidation() {
		return modelingAndValidation;
	}

	public void setModelingAndValidation(
			ModelingAndValidation modelingAndValidation) {
		this.modelingAndValidation = modelingAndValidation;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the allocationMethod-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The allocation method of the process
	 *         </p>
	 */
	public AllocationMethod getAllocationMethod() {
		return allocationMethod;
	}

	@Override
	public String getCategoryId() {
		return categoryId;
	}

	@Override
	public Process copy() {
		return new ProcessCopy().create(this);
	}

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Searches for an exchange with the given id
	 * 
	 * @param id
	 *            The id of the searched exchange
	 * @return The exchange with the given id or null if not found
	 */
	public Exchange getExchange(String id) {
		if (id == null)
			return null;
		for (Exchange e : exchanges) {
			if (id.equals(e.getId()))
				return e;
		}
		return null;
	}

	public Exchange[] getExchanges() {
		return exchanges.toArray(new Exchange[exchanges.size()]);
	}

	public Exchange[] getExchanges(FlowType flowType) {
		List<Exchange> exchanges = new ArrayList<>();
		for (Exchange exchange : getExchanges()) {
			if (exchange.getFlow().getFlowType() == flowType) {
				exchanges.add(exchange);
			}
		}
		return exchanges.toArray(new Exchange[exchanges.size()]);
	}

	public String getGeographyComment() {
		return geographyComment;
	}

	@Override
	public String getUUID() {
		return getId();
	}

	/**
	 * Due to legacy troubles requires certain object state (adminInfo) to be
	 * set manually. Database not available for loading here and since not all
	 * places that change adminInfo necessarily set the adminInfo to its holder
	 * loading it via JPA reference also doesn't work (reliably).
	 */
	@Override
	public String getVersion() {
		if (getAdminInfo() == null) {
			throw new IllegalStateException(
					"AdminInfo must be set manually before calling getVersion");
		}
		if (adminInfo != null && !Strings.nullOrEmpty(adminInfo.getVersion())) {
			return adminInfo.getVersion();
		}
		return "1.0";
	}

	public Exchange[] getInputs() {
		List<Exchange> exchanges = new ArrayList<>();
		for (Exchange exchange : getExchanges()) {
			if (exchange.isInput()) {
				exchanges.add(exchange);
			}
		}
		return exchanges.toArray(new Exchange[exchanges.size()]);
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the inputs of the specific flow type
	 * 
	 * @param flowType
	 *            The flow type
	 * 
	 * @return All inputs of the process which underlying flow has the given
	 *         flow type
	 *         </p>
	 */
	public Exchange[] getInputs(FlowType flowType) {
		List<Exchange> exchanges = new ArrayList<>();
		for (Exchange exchange : getInputs()) {
			if (exchange.getFlow().getFlowType() == flowType) {
				exchanges.add(exchange);
			}
		}
		return exchanges.toArray(new Exchange[exchanges.size()]);
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the location-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The location of the process
	 *         </p>
	 */
	public Location getLocation() {
		return location;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the outputs
	 * 
	 * @return All outputs of the process
	 *         </p>
	 */
	public Exchange[] getOutputs() {
		List<Exchange> exchanges = new ArrayList<>();
		for (Exchange exchange : getExchanges()) {
			if (!exchange.isInput()) {
				exchanges.add(exchange);
			}
		}
		return exchanges.toArray(new Exchange[exchanges.size()]);
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the outputs of the specific flow type
	 * 
	 * @param flowType
	 *            The flow type
	 * 
	 * @return All outputs of the process which underlying flow has the given
	 *         flow type
	 *         </p>
	 */
	public Exchange[] getOutputs(FlowType flowType) {
		List<Exchange> exchanges = new ArrayList<>();
		for (Exchange exchange : getOutputs()) {
			Flow flow = exchange.getFlow();
			if (flow != null && flow.getFlowType() == flowType) {
				exchanges.add(exchange);
			}
		}
		return exchanges.toArray(new Exchange[exchanges.size()]);
	}

	@Override
	public Parameter[] getParameters() {
		return parameters.toArray(new Parameter[parameters.size()]);
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the processType-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The type of the process
	 *         </p>
	 */
	public ProcessType getProcessType() {
		return processType;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the quantitativeReference-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The quantitative reference of the process
	 *         </p>
	 */
	public Exchange getQuantitativeReference() {
		return quantitativeReference;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the infrastructureProcess-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         Indicates if the process is an infrastructure process
	 *         </p>
	 */
	public boolean isInfrastructureProcess() {
		return infrastructureProcess;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		support.firePropertyChange(evt);
	}

	/**
	 * <p style="margin-top: 0">
	 * Removes an exchange from the process
	 * 
	 * @param exchange
	 *            The exchange to be removed
	 *            </p>
	 */
	public void remove(Exchange exchange) {
		if (exchange != null) {
			exchange.removePropertyChangeListener(this);
			exchanges.remove(exchange);
		}
		support.firePropertyChange("exchanges", exchange, null);
	}

	public void removeAllExchanges() {
		for (Exchange e : getExchanges()) {
			remove(e);
		}
	}

	@Override
	public void remove(Parameter parameter) {
		if (parameter != null) {
			parameter.removePropertyChangeListener(this);
			parameters.remove(parameter);
		}
		support.firePropertyChange("parameters", parameter, null);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the allocationMethod-field
	 * </p>
	 * 
	 * @param allocationMethod
	 *            <p style="margin-top: 0">
	 *            The allocation method of the process
	 *            </p>
	 */
	public void setAllocationMethod(AllocationMethod allocationMethod) {
		support.firePropertyChange("allocationMethod", this.allocationMethod,
				this.allocationMethod = allocationMethod);
	}

	@Override
	public void setCategoryId(String categoryId) {
		support.firePropertyChange("categoryId", this.categoryId,
				this.categoryId = categoryId);
	}

	@Override
	public void setDescription(String description) {
		support.firePropertyChange("description", this.description,
				this.description = description);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the geographyComment-field
	 * </p>
	 * 
	 * @param geographyComment
	 *            <p style="margin-top: 0">
	 *            A comment on the geograpy of the process
	 *            </p>
	 */
	public void setGeographyComment(String geographyComment) {
		support.firePropertyChange("geographyComment", this.geographyComment,
				this.geographyComment = geographyComment);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the infrastructureProcess-field
	 * </p>
	 * 
	 * @param infrastructureProcess
	 *            <p style="margin-top: 0">
	 *            Indicates if the process is an infrastructure process
	 *            </p>
	 */
	public void setInfrastructureProcess(boolean infrastructureProcess) {
		support.firePropertyChange("infrastructureProcess",
				this.infrastructureProcess,
				this.infrastructureProcess = infrastructureProcess);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the location-field
	 * </p>
	 * 
	 * @param location
	 *            <p style="margin-top: 0">
	 *            The location of the process
	 *            </p>
	 */
	public void setLocation(Location location) {
		support.firePropertyChange("location", this.location,
				this.location = location);
	}

	@Override
	public void setName(String name) {
		support.firePropertyChange("name", this.name, this.name = name);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the processType-field
	 * </p>
	 * 
	 * @param processType
	 *            <p style="margin-top: 0">
	 *            The type of the process
	 *            </p>
	 */
	public void setProcessType(ProcessType processType) {
		support.firePropertyChange("processType", this.processType,
				this.processType = processType);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the quantitativeReference-field
	 * </p>
	 * 
	 * @param quantitativeReference
	 *            <p style="margin-top: 0">
	 *            The quantitative reference of the process
	 *            </p>
	 */
	public void setQuantitativeReference(Exchange quantitativeReference) {
		support.firePropertyChange("quantitativeReference",
				this.quantitativeReference,
				this.quantitativeReference = quantitativeReference);
	}

	@Override
	public String toString() {
		return "Process [name=" + name + ", getId()=" + getId() + "]";
	}

}
