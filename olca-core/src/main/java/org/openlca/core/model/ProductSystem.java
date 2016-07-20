package org.openlca.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_product_systems")
public class ProductSystem extends CategorizedEntity {

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	private final List<ParameterRedef> parameterRedefs = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "tbl_process_links", joinColumns = @JoinColumn(name = "f_product_system"))
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
	@CollectionTable(name = "tbl_product_system_processes", joinColumns = {
			@JoinColumn(name = "f_product_system") })
	private final Set<Long> processes = new HashSet<>();

	@Column
	public Double cutoff;

	@Override
	public ProductSystem clone() {
		ProductSystem clone = new ProductSystem();
		Util.cloneRootFields(this, clone);
		clone.setCategory(getCategory());
		clone.setReferenceExchange(getReferenceExchange());
		clone.setReferenceProcess(getReferenceProcess());
		clone.setTargetAmount(getTargetAmount());
		clone.getProcesses().addAll(getProcesses());
		for (ProcessLink processLink : getProcessLinks())
			clone.getProcessLinks().add(processLink.clone());
		for (ParameterRedef redef : getParameterRedefs())
			clone.getParameterRedefs().add(redef.clone());
		clone.setTargetFlowPropertyFactor(getTargetFlowPropertyFactor());
		clone.setTargetUnit(getTargetUnit());
		return clone;
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

	public List<ParameterRedef> getParameterRedefs() {
		return parameterRedefs;
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
