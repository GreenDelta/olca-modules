package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Exchanges are representing the inputs and outputs of flows in processes.
 */
@Entity
@Table(name = "tbl_exchanges")
public class Exchange extends AbstractEntity {

	/**
	 * Indicates whether an exchange is an avoided product or waste flow. An
	 * exchange with an avoided product flow must be set as an input and an
	 * avoided waste flow as an output in order to be handled correctly in the
	 * calculation.
	 */
	@Column(name = "avoided_product")
	public boolean isAvoided;

	/**
	 * Indicates whether the exchange is an input (= true) or output (= false).
	 */
	@Column(name = "is_input")
	public boolean isInput;

	/**
	 * The flow of the exchange.
	 */
	@OneToOne
	@JoinColumn(name = "f_flow")
	public Flow flow;

	/**
	 * The flow property (quantity) in which the amount of the exchange is
	 * given. It is a "flow property factor" because it contains also the
	 * conversion factor to the reference quantity of the flow.
	 */
	@OneToOne
	@JoinColumn(name = "f_flow_property_factor")
	public FlowPropertyFactor flowPropertyFactor;

	/**
	 * The unit in which the exchange amount is given.
	 */
	@OneToOne
	@JoinColumn(name = "f_unit")
	public Unit unit;

	/**
	 * If the exchange is an product input or waste output this field can
	 * contain a process ID which produces the respective product or treats the
	 * waste flow. This field is used when processes are automatically linked in
	 * product system graphs. A value of zero means that no link is set.
	 */
	@Column(name = "f_default_provider")
	public long defaultProviderId;

	/**
	 * The amount of the exchange.
	 */
	@Column(name = "resulting_amount_value")
	public double amount;

	/**
	 * An optional formula for the exchange amount. The evaluated value of this
	 * formula should be always stored in the amount field.
	 */
	@Column(name = "resulting_amount_formula")
	public String amountFormula;

	/**
	 * An optional base uncertainty of the data quality entry (= Pedigree matrix
	 * entry).
	 */
	@Column(name = "base_uncertainty")
	public Double baseUncertainty;

	/**
	 * The base uncertainty of the data quality entry (= Pedigree matrix entry).
	 */
	@Column(name = "dq_entry")
	public String dqEntry;

	/**
	 * An optional uncertainty distribution of the exchange amount.
	 */
	@Embedded
	public Uncertainty uncertainty;

	@Column(name = "cost_value")
	public Double costs;

	@Column(name = "cost_formula")
	public String costFormula;

	@Column(name = "description")
	public String description;

	@OneToOne
	@JoinColumn(name = "f_currency")
	public Currency currency;

	@Override
	public String toString() {
		return "Exchange [flow=" + flow + ", input=" + isInput + ",amount="
				+ amount + ", unit=" + unit + "]";
	}

	@Override
	public Exchange clone() {
		Exchange clone = new Exchange();
		clone.amountFormula = amountFormula;
		clone.amount = amount;
		clone.isAvoided = isAvoided;
		clone.baseUncertainty = baseUncertainty;
		clone.defaultProviderId = defaultProviderId;
		clone.flow = flow;
		clone.flowPropertyFactor = flowPropertyFactor;
		clone.isInput = isInput;
		clone.dqEntry = dqEntry;
		if (uncertainty != null)
			clone.uncertainty = uncertainty.clone();
		clone.unit = unit;
		clone.costs = costs;
		clone.costFormula = costFormula;
		clone.currency = currency;
		clone.description = description;
		return clone;
	}

}
