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

@Entity
@Table(name = "tbl_product_systems")
public class ProductSystem extends CategorizedEntity {

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	public final List<ParameterRedef> parameterRedefs = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "tbl_process_links", joinColumns = @JoinColumn(name = "f_product_system"))
	public final List<ProcessLink> processLinks = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "f_reference_exchange")
	public Exchange referenceExchange;

	@OneToOne
	@JoinColumn(name = "f_reference_process")
	public Process referenceProcess;

	@Column(name = "target_amount")
	public double targetAmount;

	@OneToOne
	@JoinColumn(name = "f_target_flow_property_factor")
	public FlowPropertyFactor targetFlowPropertyFactor;

	@OneToOne
	@JoinColumn(name = "f_target_unit")
	public Unit targetUnit;

	@ElementCollection
	@Column(name = "f_process")
	@CollectionTable(name = "tbl_product_system_processes", joinColumns = {
			@JoinColumn(name = "f_product_system") })
	public final Set<Long> processes = new HashSet<>();

	@Column
	public Double cutoff;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	public List<Exchange> inventory = new ArrayList<>();

	/**
	 * Initializes a product system from the given process. Note that this
	 * function does not create a linked system; it just sets the data for the
	 * quantitative reference of the system from the quantitative reference of
	 * the given process.
	 */
	public static ProductSystem from(Process p) {
		ProductSystem system = new ProductSystem();
		system.setRefId(UUID.randomUUID().toString());
		if (p == null)
			return system;
		system.setName(p.getName());
		system.processes.add(p.getId());
		system.referenceProcess = p;
		Exchange qRef = p.getQuantitativeReference();
		system.referenceExchange = qRef;
		if (qRef == null)
			return system;
		system.targetAmount = qRef.amount;
		system.targetUnit = qRef.unit;
		system.targetFlowPropertyFactor = qRef.flowPropertyFactor;
		return system;
	}

	@Override
	public ProductSystem clone() {
		ProductSystem clone = new ProductSystem();
		Util.cloneRootFields(this, clone);
		clone.setCategory(getCategory());
		clone.referenceExchange = referenceExchange;
		clone.referenceProcess = referenceProcess;
		clone.targetAmount = targetAmount;
		clone.processes.addAll(processes);
		for (ProcessLink processLink : processLinks)
			clone.processLinks.add(processLink.clone());
		for (ParameterRedef redef : parameterRedefs)
			clone.parameterRedefs.add(redef.clone());
		for (Exchange exchange : inventory)
			clone.inventory.add(exchange.clone());
		clone.targetFlowPropertyFactor = targetFlowPropertyFactor;
		clone.targetUnit = targetUnit;
		return clone;
	}

}
