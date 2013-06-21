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
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
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
public class ProductSystem extends RootEntity implements IParameterisable {

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	private final List<Parameter> parameters = new ArrayList<>();

	@OneToMany
	@JoinTable(name = "tbl_product_system_processes", joinColumns = { @JoinColumn(name = "f_product_system") }, inverseJoinColumns = { @JoinColumn(name = "f_process") })
	private final List<Process> processes = new ArrayList<>();

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

	@Override
	public ProductSystem clone() {
		final ProductSystem productSystem = new ProductSystem();
		productSystem.setCategory(getCategory());
		productSystem.setDescription(getDescription());
		productSystem.setId(UUID.randomUUID().toString());
		productSystem.setName(getName());
		productSystem.setReferenceExchange(getReferenceExchange());
		productSystem.setReferenceProcess(getReferenceProcess());
		productSystem.setTargetAmount(getTargetAmount());
		for (final Process process : getProcesses()) {
			productSystem.getProcesses().add(process);
		}
		for (final ProcessLink processLink : getProcessLinks()) {
			productSystem.getProcessLinks().add(processLink.clone());
		}
		for (final Parameter parameter : getParameters()) {
			final Parameter p = new Parameter(UUID.randomUUID().toString(),
					new Expression(parameter.getExpression().getFormula(),
							parameter.getExpression().getValue()),
					ParameterType.PRODUCT_SYSTEM, productSystem.getId());
			p.setDescription(parameter.getDescription());
			p.setName(parameter.getName());
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
	public ProcessLink[] getIncomingLinks(final String processId) {
		final List<ProcessLink> incoming = new ArrayList<>();
		for (final ProcessLink link : getProcessLinks(processId)) {
			if (link.getRecipientProcess().getId().equals(processId)) {
				incoming.add(link);
			}
		}
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
	public ProcessLink[] getOutgoingLinks(final String processId) {
		final List<ProcessLink> outgoing = new ArrayList<>();
		for (final ProcessLink link : getProcessLinks(processId)) {
			if (link.getProviderProcess().getId().equals(processId)) {
				outgoing.add(link);
			}
		}
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

	public List<Parameter> getParameters() {
		return parameters;
	}

	public List<Process> getProcesses() {
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
