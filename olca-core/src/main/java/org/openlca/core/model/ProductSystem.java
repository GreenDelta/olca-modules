package org.openlca.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.openlca.commons.Strings;
import org.openlca.core.matrix.index.TechFlow;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_product_systems")
public final class ProductSystem extends RootEntity
		implements CalculationTarget {

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

	@JoinColumn(name = "f_product_system")
	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
	public final List<ParameterRedefSet> parameterSets = new ArrayList<>();

	@JoinColumn(name = "f_product_system")
	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
	public final List<AnalysisGroup> analysisGroups = new ArrayList<>();

	public static ProductSystem of(Process p) {
		var name = p.name;
		if (p.location != null && Strings.isNotBlank(p.location.code)) {
			var suffix = " - " + p.location.code;
			if (name != null && !name.endsWith(suffix)) {
				name += suffix;
			}
		}
		return of(name, p);
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
				link.providerType = ProviderType.PROCESS;
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

	public ProductSystem link(TechFlow techFlow, Process process) {
		if (techFlow == null || process == null)
			return this;
		processes.add(techFlow.providerId());
		processes.add(process.id);
		boolean isWaste = techFlow.isWaste();
		for (var e : process.exchanges) {
			if (e.flow == null || e.flow.id != techFlow.flowId())
				continue;
			if (e.isInput == isWaste)
				continue;
			var link = new ProcessLink();
			link.processId = process.id;
			link.flowId = e.flow.id;
			link.exchangeId = e.id;
			link.providerType = techFlow.providerType();
			link.providerId = techFlow.providerId();
			processLinks.add(link);
		}
		return this;
	}

	@Override
	public ProductSystem copy() {
		var copy = new ProductSystem();
		Entities.copyFields(this, copy);
		copy.referenceExchange = referenceExchange;
		copy.referenceProcess = referenceProcess;
		copy.targetAmount = targetAmount;
		copy.targetFlowPropertyFactor = targetFlowPropertyFactor;
		copy.targetUnit = targetUnit;
		copy.processes.addAll(processes);
		for (var link : processLinks) {
			copy.processLinks.add(link.copy());
		}
		for (var s : parameterSets) {
			copy.parameterSets.add(s.copy());
		}
		for (var ag : analysisGroups) {
			copy.analysisGroups.add(ag.copy());
		}
		return copy;
	}

}
