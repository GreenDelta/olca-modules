package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;

@MappedSuperclass
public class AbstractExchange extends AbstractEntity {

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
	 * The amount of the exchange.
	 */
	@Column(name = "resulting_amount_value")
	public double amount;

	@OneToOne
	@JoinColumn(name = "f_location")
	public Location location;

	@Column(name = "description")
	public String description;

}
