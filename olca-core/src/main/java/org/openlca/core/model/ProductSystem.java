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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * <p style="margin-top: 0">
 * A product system is a connection between different processes which at the end
 * produces one product
 * </p>
 */
@Entity
@Table(name = "tbl_product_systems")
public class ProductSystem extends CategorizedEntity implements
		IParameterisable {

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	private final List<Parameter> parameters = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_product_system")
	private final List<ProcessLink> processLinks = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "f_reference_exchange")
	private Exchange referenceExchange;

	@OneToOne
	@JoinColumn(name = "f_reference_process")
	private Process referenceProcess;

	@Column(name = "target_amount")
	private double targetAmount;

	@OneToOne
	@JoinColumn(name = "f_target_flow_property_factor")
	private FlowPropertyFactor targetFlowPropertyFactor;

	@OneToOne
	@JoinColumn(name = "f_target_unit")
	private Unit targetUnit;

	@ElementCollection
	@Column(name = "f_process")
	@CollectionTable(name = "tbl_product_system_processes", joinColumns = { @JoinColumn(name = "f_product_system") })
	private final Set<Long> processes = new HashSet<>();

	@Override
	public ProductSystem clone() {
		final ProductSystem productSystem = new ProductSystem();
		productSystem.setCategory(getCategory());
		productSystem.setDescription(getDescription());
		productSystem.setRefId(UUID.randomUUID().toString());
		productSystem.setName(getName());
		productSystem.setReferenceExchange(getReferenceExchange());
		productSystem.setReferenceProcess(getReferenceProcess());
		productSystem.setTargetAmount(getTargetAmount());
		for (Long process : getProcesses())
			productSystem.getProcesses().add(process);
		for (ProcessLink processLink : getProcessLinks())
			productSystem.getProcessLinks().add(processLink.clone());
		for (Parameter parameter : getParameters()) {
			Parameter p = new Parameter();
			p.setDescription(parameter.getDescription());
			p.setName(parameter.getName());
			p.setType(ParameterType.PRODUCT_SYSTEM);
			p.getExpression().setValue(parameter.getExpression().getValue());
			p.getExpression()
					.setFormula(parameter.getExpression().getFormula());
			productSystem.getParameters().add(p);
		}
		productSystem
				.setTargetFlowPropertyFactor(getTargetFlowPropertyFactor());
		productSystem.setTargetUnit(getTargetUnit());
		return productSystem;
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
	public ProcessLink[] getIncomingLinks(long processId) {
		List<ProcessLink> incoming = new ArrayList<>();
		for (ProcessLink link : getProcessLinks(processId))
			if (link.getRecipientProcessId() == processId)
				incoming.add(link);
		return incoming.toArray(new ProcessLink[incoming.size()]);
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
	public ProcessLink[] getOutgoingLinks(long processId) {
		List<ProcessLink> outgoing = new ArrayList<>();
		for (ProcessLink link : getProcessLinks(processId))
			if (link.getProviderProcessId() == processId)
				outgoing.add(link);
		return outgoing.toArray(new ProcessLink[outgoing.size()]);
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
	public ProcessLink[] getProcessLinks(long processId) {
		List<ProcessLink> processLinks = new ArrayList<>();
		for (ProcessLink processLink : getProcessLinks())
			if (processLink.getProviderProcessId() == processId
					|| processLink.getRecipientProcessId() == processId)
				processLinks.add(processLink);
		return processLinks.toArray(new ProcessLink[processLinks.size()]);
	}

	public Exchange getReferenceExchange() {
		return referenceExchange;
	}

	public Process getReferenceProcess() {
		return referenceProcess;
	}

	public double getTargetAmount() {
		return targetAmount;
	}

	public FlowPropertyFactor getTargetFlowPropertyFactor() {
		return targetFlowPropertyFactor;
	}

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

	public List<Parameter> getParameters() {
		return parameters;
	}

	public Set<Long> getProcesses() {
		return processes;
	}

	public List<ProcessLink> getProcessLinks() {
		return processLinks;
	}

	public void setReferenceExchange(Exchange referenceExchange) {
		this.referenceExchange = referenceExchange;
	}

	public void setReferenceProcess(Process referenceProcess) {
		this.referenceProcess = referenceProcess;
	}

	public void setTargetAmount(double targetAmount) {
		this.targetAmount = targetAmount;
	}

	public void setTargetFlowPropertyFactor(
			FlowPropertyFactor targetFlowPropertyFactor) {
		this.targetFlowPropertyFactor = targetFlowPropertyFactor;
	}

	public void setTargetUnit(Unit targetUnit) {
		this.targetUnit = targetUnit;
	}

}
