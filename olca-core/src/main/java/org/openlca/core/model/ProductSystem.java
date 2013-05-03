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
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.openlca.core.model.modelprovider.IParameterizedComponent;

/**
 * <p style="margin-top: 0">
 * A product system is a connection between different processes which at the end
 * produces one product
 * </p>
 */
@Entity
@Table(name = "tbl_productsystems")
public class ProductSystem extends AbstractEntity implements
		Copyable<ProductSystem>, IParameterizedComponent,
		PropertyChangeListener {

	@Column(length = 36, name = "categoryid")
	private String categoryId;

	@Lob
	@Column(name = "description")
	private String description;

	/**
	 * Contains the ids of the special marked processes separated by semicolon
	 */
	@Column(name = "marked")
	private String marked;

	/**
	 * A list of ids of processes which are marked in the product system
	 */
	@Transient
	private final List<String> markedList = new ArrayList<>();

	@Column(name = "name")
	private String name;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "f_owner")
	private final List<Parameter> parameters = new ArrayList<>();

	@OneToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "tbl_productsystem_process", joinColumns = { @JoinColumn(name = "f_productsystem") }, inverseJoinColumns = { @JoinColumn(name = "f_process") })
	private final List<Process> processes = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "f_productsystem")
	private final List<ProcessLink> processLinks = new ArrayList<>();

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_referenceexchange")
	private Exchange referenceExchange;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_referenceprocess")
	private Process referenceProcess;

	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	@Column(name = "targetamount")
	private double targetAmount;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_targetflowpropertyfactor")
	private FlowPropertyFactor targetFlowPropertyFactor;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_targetunit")
	private Unit targetUnit;

	public ProductSystem() {
	}

	/**
	 * Creates a new product system with the given id and name
	 */
	public ProductSystem(final String id, final String name) {
		setId(id);
		this.name = name;
	}

	/**
	 * Initialises the property change listener after object is loaded from
	 * database and builds the marked list from the string stored in the
	 * database
	 */
	@PostLoad
	protected void postLoad() {
		if (marked != null) {
			for (final String s : marked.split(";")) {
				if (s.length() > 0) {
					markedList.add(s);
				}
			}
		}
		marked = "";
		for (final Parameter parameter : getParameters()) {
			parameter.addPropertyChangeListener(this);
		}
	}

	/**
	 * Concatenates the ids in the marked list
	 */
	@PrePersist
	@PreUpdate
	protected void prePersist() {
		marked = "";
		for (int i = 0; i < markedList.size(); i++) {
			marked += markedList.get(i);
			if (i != markedList.size() - 1) {
				marked += ";";
			}
		}
	}

	@Override
	public void add(final Parameter parameter) {
		if (!parameters.contains(parameter)) {
			parameters.add(parameter);
			support.firePropertyChange("parameters", null, parameter);
			parameter.addPropertyChangeListener(this);
		}
	}

	/**
	 * <p style="margin-top: 0">
	 * Adds a process to the product system
	 * 
	 * @param process
	 *            The process to be added
	 *            </p>
	 */
	public void add(final Process process) {
		if (!processes.contains(process)) {
			processes.add(process);
			support.firePropertyChange("processes", null, process);
		}
	}

	/**
	 * <p style="margin-top: 0">
	 * Adds a process link to the product system
	 * 
	 * @param processLink
	 *            The process link to be added
	 *            </p>
	 */
	public void add(final ProcessLink processLink) {
		if (!processLinks.contains(processLink)) {
			processLinks.add(processLink);
			support.firePropertyChange("processLinks", null, processLink);
			processLink.addPropertyChangeListener(this);
		}
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
	public ProductSystem copy() {
		final ProductSystem productSystem = new ProductSystem();
		productSystem.setCategoryId(getCategoryId());
		productSystem.setDescription(getDescription());
		productSystem.setId(UUID.randomUUID().toString());
		productSystem.setName(getName());
		productSystem.setReferenceExchange(getReferenceExchange());
		productSystem.setReferenceProcess(getReferenceProcess());
		productSystem.setTargetAmount(getTargetAmount());
		for (final Process process : getProcesses()) {
			productSystem.add(process);
		}
		for (final ProcessLink processLink : getProcessLinks()) {
			productSystem.add(processLink.copy());
		}
		for (final Parameter parameter : getParameters()) {
			final Parameter p = new Parameter(UUID.randomUUID().toString(),
					new Expression(parameter.getExpression().getFormula(),
							parameter.getExpression().getValue()),
					ParameterType.PRODUCT_SYSTEM, productSystem.getId());
			p.setDescription(parameter.getDescription());
			p.setName(parameter.getName());
			productSystem.add(p);
		}
		productSystem
				.setTargetFlowPropertyFactor(getTargetFlowPropertyFactor());
		productSystem.setTargetUnit(getTargetUnit());
		return productSystem;
	}

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * <p style="margin-top: 0">
	 * Searches for links where the process with the given id is recipient
	 * </p>
	 * 
	 * @param processId
	 *            The id of the process to search links for
	 * 
	 * @return <p style="margin-top: 0">
	 *         The links where the process with the given id is recipient
	 *         </p>
	 */
	public ProcessLink[] getIncomingLinks(final String processId) {
		final List<ProcessLink> incoming = new ArrayList<>();
		for (final ProcessLink link : getProcessLinks(processId)) {
			if (link.getRecipientProcess().getId().equals(processId)) {
				incoming.add(link);
			}
		}
		return incoming.toArray(new ProcessLink[incoming.size()]);
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * <p style="margin-top: 0">
	 * Searches for links where the process with the given id is provider
	 * </p>
	 * 
	 * @param processId
	 *            The id of the process to search links for
	 * 
	 * @return <p style="margin-top: 0">
	 *         The links where the process with the given id is provider
	 *         </p>
	 */
	public ProcessLink[] getOutgoingLinks(final String processId) {
		final List<ProcessLink> outgoing = new ArrayList<>();
		for (final ProcessLink link : getProcessLinks(processId)) {
			if (link.getProviderProcess().getId().equals(processId)) {
				outgoing.add(link);
			}
		}
		return outgoing.toArray(new ProcessLink[outgoing.size()]);
	}

	@Override
	public Parameter[] getParameters() {
		return parameters.toArray(new Parameter[parameters.size()]);
	}

	/**
	 * Returns the process with the specific id
	 * 
	 * @param id
	 *            The if of the requested process
	 * @return The process with the specific id if found, null otherwise
	 */
	public Process getProcess(final String id) {
		Process process = null;
		int i = 0;
		while (process == null && i < getProcesses().length) {
			if (getProcesses()[i].getId().equals(id)) {
				process = getProcesses()[i];
			} else {
				i++;
			}
		}
		return process;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the processes
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The processes of the product system
	 *         </p>
	 */
	public Process[] getProcesses() {
		return processes.toArray(new Process[processes.size()]);
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the process links
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The process links of the product system
	 *         </p>
	 */
	public ProcessLink[] getProcessLinks() {
		return processLinks.toArray(new ProcessLink[processLinks.size()]);
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the process links for the given process id
	 * 
	 * @param processId
	 *            The id of the process to search process links for
	 * 
	 * @return All process links having the process with the given id as
	 *         provider or recipient
	 *         </p>
	 */
	public ProcessLink[] getProcessLinks(final String processId) {
		final List<ProcessLink> processLinks = new ArrayList<>();
		for (final ProcessLink processLink : getProcessLinks()) {
			if (processLink.getProviderProcess().getId().equals(processId)
					|| processLink.getRecipientProcess().getId()
							.equals(processId)) {
				processLinks.add(processLink);
			}
		}
		return processLinks.toArray(new ProcessLink[processLinks.size()]);
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the referenceExchange-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The product to be produced by the product system
	 *         </p>
	 */
	public Exchange getReferenceExchange() {
		return referenceExchange;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the referenceProcess-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The reference process, that produces the product of the product
	 *         system
	 *         </p>
	 */
	public Process getReferenceProcess() {
		return referenceProcess;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the targetAmount-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The amount to be produced by the product system
	 *         </p>
	 */
	public double getTargetAmount() {
		return targetAmount;
	}

	/**
	 * Getter of the target flow property factor
	 * 
	 * @return The target flow property factor
	 */
	public FlowPropertyFactor getTargetFlowPropertyFactor() {
		return targetFlowPropertyFactor;
	}

	/**
	 * Getter of the target unit
	 * 
	 * @return The target unit
	 */
	public Unit getTargetUnit() {
		return targetUnit;
	}

	/**
	 * Getter of the converted target amount
	 * 
	 * @return The target amount multiplied with the unit conversion factor
	 *         divided by the flow property conversion factor
	 */
	public double getConvertedTargetAmount() {
		return targetAmount / targetFlowPropertyFactor.getConversionFactor()
				* targetUnit.getConversionFactor();
	}

	/**
	 * Indicates if a process is marked
	 * 
	 * @param processId
	 *            The id of the process to check
	 * @return True if the process is marked, false otherwise
	 */
	public boolean isMarked(final String processId) {
		return markedList.contains(processId);
	}

	/**
	 * Marks a specific process in the product system
	 * 
	 * @param processId
	 *            The id of the process to mark
	 */
	public void mark(final String processId) {
		if (!markedList.contains(processId)) {
			markedList.add(processId);
			support.firePropertyChange("marked", false, true);
		}
	}

	/**
	 * Converts all exchanges of all processes to their reference flow property
	 * and unit
	 */
	public void normalize() {
		for (final Process process : getProcesses()) {
			for (final Exchange exchange : process.getExchanges()) {
				exchange.getResultingAmount().setValue(
						exchange.getConvertedResult());
				exchange.getResultingAmount().setFormula(
						Double.toString(exchange.getResultingAmount()
								.getValue()));
			}
		}
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		support.firePropertyChange(evt);
	}

	@Override
	public void remove(final Parameter parameter) {
		parameters.remove(parameter);
		parameter.removePropertyChangeListener(this);
		support.firePropertyChange("parameters", parameter, null);
	}

	/**
	 * <p style="margin-top: 0">
	 * Removes a process from the product system
	 * 
	 * @param process
	 *            The process to be removed
	 *            </p>
	 */
	public void remove(final Process process) {
		if (process != null) {
			if (markedList.contains(process.getId())) {
				markedList.remove(process.getId());
			}
			processes.remove(process);
		}
		support.firePropertyChange("processes", process, null);
	}

	/**
	 * <p style="margin-top: 0">
	 * Removes a process link from the product system
	 * 
	 * @param processLink
	 *            The process link to be removed
	 *            </p>
	 */
	public void remove(final ProcessLink processLink) {
		processLink.removePropertyChangeListener(this);
		processLinks.remove(processLink);
		support.firePropertyChange("processLinks", processLink, null);
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

	@Override
	public void setName(final String name) {
		support.firePropertyChange("name", this.name, this.name = name);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the referenceExchange-field
	 * </p>
	 * 
	 * @param referenceExchange
	 *            <p style="margin-top: 0">
	 *            The product to be produced by the product system
	 *            </p>
	 */
	public void setReferenceExchange(final Exchange referenceExchange) {
		support.firePropertyChange("referenceExchange", this.referenceExchange,
				this.referenceExchange = referenceExchange);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the referenceProcess-field
	 * </p>
	 * 
	 * @param referenceProcess
	 *            <p style="margin-top: 0">
	 *            The reference process, that produces the product of the
	 *            product system
	 *            </p>
	 */
	public void setReferenceProcess(final Process referenceProcess) {
		support.firePropertyChange("referenceProcess", this.referenceProcess,
				this.referenceProcess = referenceProcess);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the targetAmount-field
	 * </p>
	 * 
	 * @param targetAmount
	 *            <p style="margin-top: 0">
	 *            The amount to be produced by the product system
	 *            </p>
	 */
	public void setTargetAmount(final double targetAmount) {
		support.firePropertyChange("targetAmount", this.targetAmount,
				this.targetAmount = targetAmount);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the targetFlowPropertyFactor-field
	 * </p>
	 * 
	 * @param targetFlowPropertyFactor
	 *            <p style="margin-top: 0">
	 *            The target flow property factor of the product system
	 *            </p>
	 */
	public void setTargetFlowPropertyFactor(
			final FlowPropertyFactor targetFlowPropertyFactor) {
		support.firePropertyChange("targetFlowPropertyFactor",
				this.targetFlowPropertyFactor,
				this.targetFlowPropertyFactor = targetFlowPropertyFactor);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the targetUnit-field
	 * </p>
	 * 
	 * @param targetUnit
	 *            <p style="margin-top: 0">
	 *            The target unit of the product system
	 *            </p>
	 */
	public void setTargetUnit(final Unit targetUnit) {
		support.firePropertyChange("targetUnit", this.targetUnit,
				this.targetUnit = targetUnit);
	}

	/**
	 * Unmarks a specific process in the product system
	 * 
	 * @param processId
	 *            The id of the process to unmark
	 */
	public void unmark(final String processId) {
		if (markedList.contains(processId)) {
			markedList.remove(processId);
			support.firePropertyChange("marked", true, false);
		}
	}
}
