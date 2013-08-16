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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * <p style="margin-top: 0">
 * A process contains a set of incoming and outgoing flows
 * </p>
 */
@Entity
@Table(name = "tbl_processes")
public class Process extends CategorizedEntity implements IParameterisable {

	@Column(name = "default_allocation_method")
	@Enumerated(EnumType.STRING)
	private AllocationMethod defaultAllocationMethod;

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_process")
	private final List<AllocationFactor> allocationFactors = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	private List<Exchange> exchanges = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "f_location")
	private Location location;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	private final List<Parameter> parameters = new ArrayList<>();

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_process_doc")
	private ProcessDocumentation documentation;

	@Column(name = "process_type")
	@Enumerated(EnumType.STRING)
	private ProcessType processType = ProcessType.UNIT_PROCESS;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_quantitative_reference")
	private Exchange quantitativeReference;

	@Column(name = "infrastructure_process")
	private boolean infrastructureProcess;

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

	public ProcessDocumentation getDocumentation() {
		return documentation;
	}

	public void setDocumentation(ProcessDocumentation documentation) {
		this.documentation = documentation;
	}

	@Override
	public Process clone() {
		return new ProcessCopy().create(this);
	}

	public Exchange[] getExchanges(FlowType... flowTypes) {
		if (flowTypes == null)
			return exchanges.toArray(new Exchange[exchanges.size()]);
		List<Exchange> exchanges = new ArrayList<>();
		for (Exchange exchange : getExchanges())
			for (FlowType flowType : flowTypes)
				if (exchange.getFlow().getFlowType() == flowType) {
					exchanges.add(exchange);
					break;
				}
		return exchanges.toArray(new Exchange[exchanges.size()]);
	}

	public Exchange[] getInputs() {
		List<Exchange> exchanges = new ArrayList<>();
		for (Exchange exchange : getExchanges())
			if (exchange.isInput())
				exchanges.add(exchange);
		return exchanges.toArray(new Exchange[exchanges.size()]);
	}

	public Exchange[] getInputs(FlowType... flowTypes) {
		if (flowTypes == null)
			return getInputs();
		List<Exchange> exchanges = new ArrayList<>();
		for (Exchange exchange : getExchanges())
			if (exchange.isInput())
				for (FlowType flowType : flowTypes)
					if (exchange.getFlow().getFlowType() == flowType) {
						exchanges.add(exchange);
						break;
					}
		return exchanges.toArray(new Exchange[exchanges.size()]);
	}

	public Location getLocation() {
		return location;
	}

	public Exchange[] getOutputs() {
		List<Exchange> exchanges = new ArrayList<>();
		for (Exchange exchange : getExchanges())
			if (!exchange.isInput())
				exchanges.add(exchange);
		return exchanges.toArray(new Exchange[exchanges.size()]);
	}

	public Exchange[] getOutputs(FlowType... flowTypes) {
		if (flowTypes == null)
			return getOutputs();
		List<Exchange> exchanges = new ArrayList<>();
		for (Exchange exchange : getExchanges())
			if (!exchange.isInput())
				for (FlowType flowType : flowTypes)
					if (exchange.getFlow().getFlowType() == flowType) {
						exchanges.add(exchange);
						break;
					}
		return exchanges.toArray(new Exchange[exchanges.size()]);
	}

	public ProcessType getProcessType() {
		return processType;
	}

	public Exchange getQuantitativeReference() {
		return quantitativeReference;
	}

	public List<Exchange> getExchanges() {
		return exchanges;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public boolean isInfrastructureProcess() {
		return infrastructureProcess;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setProcessType(ProcessType processType) {
		this.processType = processType;
	}

	public void setQuantitativeReference(Exchange quantitativeReference) {
		this.quantitativeReference = quantitativeReference;
	}

	public void setInfrastructureProcess(boolean infrastructureProcess) {
		this.infrastructureProcess = infrastructureProcess;
	}

	public AllocationMethod getDefaultAllocationMethod() {
		return defaultAllocationMethod;
	}

	public void setDefaultAllocationMethod(AllocationMethod method) {
		this.defaultAllocationMethod = method;
	}

	public List<AllocationFactor> getAllocationFactors() {
		return allocationFactors;
	}

}
