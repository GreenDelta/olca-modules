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
import javax.persistence.Transient;

@Entity
@Table(name = "tbl_product_systems")
public class ProductSystem extends CategorizedEntity {

	/**
	 * TODO: This is currently an experimental feature and indicates that the
	 * product system does not store the supply chain network of the reference
	 * process but is rather a calculation setup. When calculating such a
	 * product system we directly take the product and waste flows between the
	 * processes to build the network/matrices for the calculation. Note that
	 * this field is currently not stored in the database. In a later stage we
	 * may support this as a specific product system type directly in openLCA.
	 */
	@Transient
	public boolean withoutNetwork = false;

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
	 * the given process which needs to be a product output or waste input.
	 */
	public static ProductSystem from(Process p) {
		ProductSystem system = new ProductSystem();
		system.refId = UUID.randomUUID().toString();
		if (p == null)
			return system;
		system.name = p.name;
		system.processes.add(p.id);
		system.referenceProcess = p;
		Exchange qRef = p.quantitativeReference;
		system.referenceExchange = qRef;
		if (qRef == null || qRef.flow == null)
			return system;
		FlowType type = qRef.flow.flowType;
		if (qRef.isInput && type != FlowType.WASTE_FLOW)
			return system;
		if (!qRef.isInput && type != FlowType.PRODUCT_FLOW)
			return system;
		system.targetAmount = qRef.amount;
		system.targetUnit = qRef.unit;
		system.targetFlowPropertyFactor = qRef.flowPropertyFactor;
		return system;
	}

	@Override
	public ProductSystem clone() {
		ProductSystem clone = new ProductSystem();
		Util.copyRootFields(this, clone);
		clone.category = category;
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
