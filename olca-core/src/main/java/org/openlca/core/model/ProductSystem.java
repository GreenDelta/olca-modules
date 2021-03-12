package org.openlca.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

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

	/**
	 * @deprecated parameter redefinitions are now organized in parameter sets
	 */
	@Deprecated
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	public final List<ParameterRedef> parameterRedefs = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "tbl_process_links",
			joinColumns = @JoinColumn(name = "f_product_system"))
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
			@JoinColumn(name = "f_product_system")})
	public final Set<Long> processes = new HashSet<>();

	@Column
	public Double cutoff;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	public final List<Exchange> inventory = new ArrayList<>();

	@JoinColumn(name = "f_product_system")
	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
	public final List<ParameterRedefSet> parameterSets = new ArrayList<>();

	public static ProductSystem of(Process p) {
		return of(p.name, p);
	}

	/**
	 * Initializes a product system from the given process. Note that this
	 * function does not create a linked system; it just sets the data for the
	 * quantitative reference of the system from the quantitative reference of
	 * the given process which needs to be a product output or waste input.
	 */
	public static ProductSystem of(String name, Process p) {
		var system = new ProductSystem();
		Entities.init(system, name);
		system.processes.add(p.id);
		system.referenceProcess = p;
		var qRef = p.quantitativeReference;
		system.referenceExchange = qRef;
		if (qRef == null)
			return system;
		system.targetAmount = qRef.amount;
		system.targetUnit = qRef.unit;
		system.targetFlowPropertyFactor = qRef.flowPropertyFactor;
		return system;
	}

	/**
	 * Links the first matching output of the given provider with a
	 * corresponding input of the given recipient.
	 */
	public ProductSystem link(Process provider, Process recipient) {
		if (provider == null || recipient == null)
			return this;
		processes.add(provider.id);
		processes.add(recipient.id);
		for (var output : provider.exchanges) {
			if (output.isInput
					|| output.flow == null
					|| output.flow.flowType == FlowType.ELEMENTARY_FLOW)
				continue;
			for (var input : recipient.exchanges) {
				if (!input.isInput
						|| !Objects.equals(output.flow, input.flow))
					continue;
				var link = new ProcessLink();
				link.flowId = output.flow.id;
				link.isSystemLink = false;
				if (output.flow.flowType == FlowType.WASTE_FLOW) {
					link.processId = provider.id;
					link.exchangeId = output.id;
					link.providerId = recipient.id;
				} else {
					link.processId = recipient.id;
					link.exchangeId = input.id;
					link.providerId = provider.id;
				}
				processLinks.add(link);
			}
		}
		return this;
	}

	@Override
	public ProductSystem clone() {
		var clone = new ProductSystem();
		Entities.copyFields(this, clone);
		clone.referenceExchange = referenceExchange;
		clone.referenceProcess = referenceProcess;
		clone.targetAmount = targetAmount;
		clone.processes.addAll(processes);
		for (ProcessLink link : processLinks) {
			clone.processLinks.add(link.clone());
		}
		for (ParameterRedef p : parameterRedefs) {
			clone.parameterRedefs.add(p.clone());
		}
		for (ParameterRedefSet s : parameterSets) {
			clone.parameterSets.add(s.clone());
		}
		for (Exchange exchange : inventory) {
			clone.inventory.add(exchange.clone());
		}
		clone.targetFlowPropertyFactor = targetFlowPropertyFactor;
		clone.targetUnit = targetUnit;
		return clone;
	}

}
