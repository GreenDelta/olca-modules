package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Exchanges are representing the inputs and outputs of flows in processes.
 */
@Entity
@Table(name = "tbl_exchanges")
public class Exchange extends AbstractExchange implements Copyable<Exchange> {

	/**
	 * Indicates whether an exchange is an avoided product or waste flow. An
	 * exchange with an avoided product flow must be set as an input and an
	 * avoided waste flow as an output in order to be handled correctly in the
	 * calculation.
	 */
	@Column(name = "avoided_product")
	public boolean isAvoided;

	/**
	 * An id that is unique within the process, this id must not be changed
	 * after creation
	 */
	@Column(name = "internal_id")
	public int internalId;

	/**
	 * If the exchange is an product input or waste output this field can
	 * contain a process ID which produces the respective product or treats the
	 * waste flow. This field is used when processes are automatically linked in
	 * product system graphs. A value of zero means that no link is set.
	 */
	@Column(name = "f_default_provider")
	public long defaultProviderId;

	/**
	 * An optional formula for the exchange amount. The evaluated value of this
	 * formula should be always stored in the amount field.
	 */
	@Column(name = "resulting_amount_formula")
	public String formula;

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

	@OneToOne
	@JoinColumn(name = "f_currency")
	public Currency currency;

	@Override
	public String toString() {
		return "Exchange [flow=" + flow
				+ ", input=" + isInput
				+ ",amount=" + amount
				+ ", unit=" + unit + "]";
	}

	public static Exchange input(Flow flow, double amount) {
		var e = of(flow);
		e.amount = amount;
		e.isInput = true;
		return e;
	}

	public static Exchange output(Flow flow, double amount) {
		var e = of(flow);
		e.amount = amount;
		e.isInput = false;
		return e;
	}

	public static Exchange of(Flow flow) {
		var e = new Exchange();
		e.flow = flow;
		e.unit = flow.getReferenceUnit();
		e.flowPropertyFactor = flow.getReferenceFactor();
		e.amount = 1.0;
		return e;
	}

	public static Exchange of(Flow flow, FlowProperty property, Unit unit) {
		var e = new Exchange();
		e.flow = flow;
		e.unit = unit;
		e.flowPropertyFactor = flow.getFactor(property);
		e.amount = 1;
		return e;
	}

	@Override
	public Exchange copy() {
		var clone = new Exchange();
		clone.internalId = internalId;
		clone.formula = formula;
		clone.amount = amount;
		clone.isAvoided = isAvoided;
		clone.baseUncertainty = baseUncertainty;
		clone.defaultProviderId = defaultProviderId;
		clone.flow = flow;
		clone.flowPropertyFactor = flowPropertyFactor;
		clone.isInput = isInput;
		clone.dqEntry = dqEntry;
		if (uncertainty != null) {
			clone.uncertainty = uncertainty.copy();
		}
		clone.unit = unit;
		clone.costs = costs;
		clone.costFormula = costFormula;
		clone.currency = currency;
		clone.description = description;
		clone.location = location;
		return clone;
	}
}
