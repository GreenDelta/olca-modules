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
		for (ParameterRedef redef : getParameterRedefs()) {
			productSystem.getParameterRedefs().add(redef.clone());
		}
		productSystem
				.setTargetFlowPropertyFactor(getTargetFlowPropertyFactor());
		productSystem.setTargetUnit(getTargetUnit());
		return productSystem;
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
