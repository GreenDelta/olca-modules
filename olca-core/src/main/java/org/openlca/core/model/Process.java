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
public class Process extends RootEntity implements IParameterisable {

	@Column(name = "allocation_method")
	@Enumerated(EnumType.STRING)
	private AllocationMethod allocationMethod;

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
	private ProcessType processType = ProcessType.UnitProcess;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_quantitative_reference")
	private Exchange quantitativeReference;

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

	public ProcessDocumentation getDocumentation() {
		return documentation;
	}

	public void setDocumentation(ProcessDocumentation documentation) {
		this.documentation = documentation;
	}

	public AllocationMethod getAllocationMethod() {
		return allocationMethod;
	}

	@Override
	public Process clone() {
		return new ProcessCopy().create(this);
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

	public Exchange[] getExchanges(FlowType flowType) {
		List<Exchange> exchanges = new ArrayList<>();
		for (Exchange exchange : getExchanges()) {
			if (exchange.getFlow().getFlowType() == flowType) {
				exchanges.add(exchange);
			}
		}
		return exchanges.toArray(new Exchange[exchanges.size()]);
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

	public Exchange[] getInputs(FlowType flowType) {
		List<Exchange> exchanges = new ArrayList<>();
		for (Exchange exchange : getInputs()) {
			if (exchange.getFlow().getFlowType() == flowType) {
				exchanges.add(exchange);
			}
		}
		return exchanges.toArray(new Exchange[exchanges.size()]);
	}

	public Location getLocation() {
		return location;
	}

	public Exchange[] getOutputs() {
		List<Exchange> exchanges = new ArrayList<>();
		for (Exchange exchange : getExchanges()) {
			if (!exchange.isInput()) {
				exchanges.add(exchange);
			}
		}
		return exchanges.toArray(new Exchange[exchanges.size()]);
	}

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

	public void setAllocationMethod(AllocationMethod allocationMethod) {
		this.allocationMethod = allocationMethod;
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

}
