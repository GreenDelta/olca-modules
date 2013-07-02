/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * An exchange is a carrier of a flow (an elementary flows like CO2 or a product
 * flow). Exchanges are representing the inputs and outputs of processes.
 */
@Entity
@Table(name = "tbl_exchanges")
public class Exchange extends AbstractEntity implements PropertyChangeListener {

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_exchange")
	private final List<AllocationFactor> allocationFactors = new ArrayList<>();

	@Column(name = "avoidedproduct")
	private boolean avoidedProduct;

	@Column(name = "distributionType")
	private UncertaintyDistributionType distributionType = UncertaintyDistributionType.NONE;

	@OneToOne
	@JoinColumn(name = "f_flow")
	private Flow flow;

	@OneToOne
	@JoinColumn(name = "f_flow_property_factor")
	private FlowPropertyFactor flowPropertyFactor;

	@Column(name = "is_input")
	private boolean input;

	@Column(name = "f_owner")
	private String ownerId;

	@Column(name = "parametrized")
	private boolean parametrized;

	@Column(name = "base_uncertainty")
	private Double baseUncertainty;

	@Column(name = "f_default_provider")
	private String defaultProviderId;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "value", column = @Column(name = "resultingamount_value")),
			@AttributeOverride(name = "formula", column = @Column(name = "resultingamount_formula")) })
	private final Expression resultingAmount = new Expression("1", 1d);

	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	/**
	 * Normal distribution: arithmetic mean value <br>
	 * Lognormal distribution: geometric mean value <br>
	 * Triangle distribution: min value <br>
	 * Uniform distribution: min value
	 */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "value", column = @Column(name = "parameter1_value")),
			@AttributeOverride(name = "formula", column = @Column(name = "parameter1_formula")) })
	private Expression uncertaintyParameter1;

	/**
	 * Normal distribution: arithmetic standard deviation<br>
	 * Lognormal distribution: geometric standard deviation<br>
	 * Triangle distribution: most likely value<br>
	 * Uniform distribution: max value
	 */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "value", column = @Column(name = "parameter2_value")),
			@AttributeOverride(name = "formula", column = @Column(name = "parameter2_formula")) })
	private Expression uncertaintyParameter2;

	/**
	 * Triangle distribution: max value
	 */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "value", column = @Column(name = "parameter3_value")),
			@AttributeOverride(name = "formula", column = @Column(name = "parameter3_formula")) })
	private Expression uncertaintyParameter3;

	@OneToOne
	@JoinColumn(name = "f_unit")
	private Unit unit;

	@Column(name = "pedigree_uncertainty")
	private String pedigreeUncertainty;

	public Exchange() {
	}

	/**
	 * Creates a new exchange
	 * 
	 * @param ownerId
	 *            The id of the owning process or lci result
	 */
	public Exchange(String ownerId) {
		this.ownerId = ownerId;
		resultingAmount.addPropertyChangeListener(this);
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	private void calculateResultingAmount(UncertaintyDistributionType type) {
		String formula = null;
		switch (type) {
		case NORMAL:
			formula = uncertaintyParameter1.getFormula();
			break;
		case LOG_NORMAL:
			formula = uncertaintyParameter1.getFormula();
			break;
		case TRIANGLE:
			formula = "(" + uncertaintyParameter1.getFormula() + " + "
					+ uncertaintyParameter2.getFormula() + " + "
					+ uncertaintyParameter3.getFormula() + ") / 3";
			break;
		case UNIFORM:
			formula = "(" + uncertaintyParameter1.getFormula() + " + "
					+ uncertaintyParameter2.getFormula() + ") / 2";
			break;
		default:
			break;
		}
		if (formula != null) {
			resultingAmount.setFormula(formula);
		}
	}

	/**
	 * Initializes the property change listener after object is loaded from
	 * database
	 */
	@PostLoad
	protected void postLoad() {
		for (final AllocationFactor factor : getAllocationFactors()) {
			factor.addPropertyChangeListener(this);
		}
		if (resultingAmount != null) {
			resultingAmount.addPropertyChangeListener(this);
		}
		if (uncertaintyParameter1 != null) {
			uncertaintyParameter1.addPropertyChangeListener(this);
		}
		if (uncertaintyParameter2 != null) {
			uncertaintyParameter2.addPropertyChangeListener(this);
		}
		if (uncertaintyParameter3 != null) {
			uncertaintyParameter3.addPropertyChangeListener(this);
		}
	}

	/**
	 * <p style="margin-top: 0">
	 * Adds an allocation factor to the exchange
	 * 
	 * @param allocationFactor
	 *            The allocation factor to be added
	 *            </p>
	 */
	public void add(final AllocationFactor allocationFactor) {
		if (!allocationFactors.contains(allocationFactor)) {
			allocationFactors.add(allocationFactor);
			support.firePropertyChange("allocationFactors", null,
					allocationFactor);
			allocationFactor.addPropertyChangeListener(this);
		}
	}

	/**
	 * <p style="margin-top: 0">
	 * Adds an allocation factor to the exchange
	 * 
	 * @param allocationFactor
	 *            The allocation factor to be added
	 * @param firePropertyChange
	 *            if true the property change support fires a property change.
	 *            This method does the same as {@link #add(AllocationFactor)} if
	 *            firePropertyChange is true
	 *            </p>
	 */
	public void add(final AllocationFactor allocationFactor,
			final boolean firePropertyChange) {
		allocationFactors.add(allocationFactor);
		if (firePropertyChange) {
			support.firePropertyChange("allocationFactors", null,
					allocationFactor);
		}
		allocationFactor.addPropertyChangeListener(this);
	}

	/**
	 * <p style="margin-top: 0">
	 * Adds a property change listener to the support
	 * 
	 * @param listener
	 *            The property change listener to be added
	 *            </p>
	 */
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	/**
	 * Searches for an allocation factor for the product with the given id
	 * 
	 * @param productId
	 *            The id of the product for which the allocation factor is
	 *            needed
	 * @return The allocation factor of the exchange for the product with the
	 *         specified id
	 */
	public AllocationFactor getAllocationFactor(final String productId) {
		AllocationFactor factor = null;
		int i = 0;
		while (factor == null && i < getAllocationFactors().length) {
			if (getAllocationFactors()[i].getProductId().equals(productId)) {
				factor = getAllocationFactors()[i];
			} else {
				i++;
			}
		}
		return factor;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the allocation factors
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The allocation factors for the causal allocation method of the
	 *         exchange
	 *         </p>
	 */
	public AllocationFactor[] getAllocationFactors() {
		return allocationFactors.toArray(new AllocationFactor[allocationFactors
				.size()]);
	}

	/**
	 * <p style="margin-top: 0">
	 * Applies the conversion factor of the unit and the flow property of the
	 * exchange onto the resulting amount and returns the result
	 * </p>
	 * 
	 * @return The converted result
	 */
	public double getConvertedResult() {
		return getResultingAmount().getValue()
				/ getFlowPropertyFactor().getConversionFactor()
				* getUnit().getConversionFactor();
	}

	/**
	 * Getter of the distributionType-field
	 * 
	 * @return The type of uncertainty distribution
	 */
	public UncertaintyDistributionType getDistributionType() {
		return distributionType;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the flow-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The flow of the exchange
	 *         </p>
	 */
	public Flow getFlow() {
		return flow;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the flowPropertyFactor-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The flow property of the exchange including the conversion factor
	 *         </p>
	 */
	public FlowPropertyFactor getFlowPropertyFactor() {
		return flowPropertyFactor;
	}

	/**
	 * Getter of the ownerId-field
	 * 
	 * @return The id of the owning process / lci result
	 */
	public String getOwnerId() {
		return ownerId;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the resultingAmount-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The resulting amount of the exchange. Will be updated from the
	 *         uncertainty distribution object if one is selected
	 *         </p>
	 */
	public Expression getResultingAmount() {
		return resultingAmount;
	}

	/**
	 * Getter of the first uncertainty parameter
	 * 
	 * @return In case of: <br>
	 *         Normal distribution: arithmetic mean value<br>
	 *         Lognormal distribution: geometric mean value<br>
	 *         Triangle distribution: min value<br>
	 *         Uniform distribution: min value<br>
	 *         null otherwise
	 */
	public Expression getUncertaintyParameter1() {
		return uncertaintyParameter1;
	}

	/**
	 * Getter of the second uncertainty parameter
	 * 
	 * @return In case of: <br>
	 *         Normal distribution: arithmetic standard deviation<br>
	 *         Lognormal distribution: geometric standard deviation<br>
	 *         Triangle distribution: most likely value<br>
	 *         Uniform distribution: max value<br>
	 *         null otherwise
	 */
	public Expression getUncertaintyParameter2() {
		return uncertaintyParameter2;
	}

	/**
	 * Getter of the third uncertainty parameter
	 * 
	 * @return In case of: <br>
	 *         Triangle distribution: max value<br>
	 *         null otherwise
	 */
	public Expression getUncertaintyParameter3() {
		return uncertaintyParameter3;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the unit-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The unit of the exchange
	 *         </p>
	 */
	public Unit getUnit() {
		return unit;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the avoidedProduct-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         Indicates if the exchange is an avoided product
	 *         </p>
	 */
	public boolean isAvoidedProduct() {
		return avoidedProduct;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the input-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         Indicates whether the exchange is an input or an output
	 *         </p>
	 */
	public boolean isInput() {
		return input;
	}

	/**
	 * Getter of the parametrized-field
	 * 
	 * @return indicates if the exchange is parametrized or not
	 */
	public boolean isParametrized() {
		return parametrized;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals("formula")) {
			parametrized = false;
			if (!parametrized) {
				final String formula = getResultingAmount().getFormula();
				try {
					Double.parseDouble(formula);
				} catch (final NumberFormatException e) {
					parametrized = true;
				}
			}
		}
		if (arg0.getSource() instanceof Expression) {
			calculateResultingAmount(this.distributionType);
		}
		support.firePropertyChange(arg0);
	}

	/**
	 * <p style="margin-top: 0">
	 * Removes an allocation factor from the exchange
	 * 
	 * @param allocationFactor
	 *            The allocation factor to be removed
	 *            </p>
	 */
	public void remove(final AllocationFactor allocationFactor) {
		allocationFactor.removePropertyChangeListener(this);
		allocationFactors.remove(allocationFactor);
		support.firePropertyChange("allocationFactors", allocationFactor, null);
	}

	/**
	 * <p style="margin-top: 0">
	 * Removes a property change listener from the support
	 * 
	 * @param listener
	 *            The property change listener to be removed
	 *            </p>
	 */
	public void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the avoidedProduct-field
	 * </p>
	 * 
	 * @param avoidedProduct
	 *            <p style="margin-top: 0">
	 *            Indicates if the exchange is an avoided product
	 *            </p>
	 */
	public void setAvoidedProduct(final boolean avoidedProduct) {
		support.firePropertyChange("avoidedProduct", this.avoidedProduct,
				this.avoidedProduct = avoidedProduct);
	}

	public void setDistributionType(UncertaintyDistributionType distributionType) {
		if (this.distributionType == distributionType)
			return;
		resetUncertainty();
		if (distributionType != null
				&& distributionType != UncertaintyDistributionType.NONE) {
			setUncertaintyValues(distributionType);
			calculateResultingAmount(distributionType);
		}
		support.firePropertyChange("distributionType", this.distributionType,
				this.distributionType = distributionType);
	}

	private void resetUncertainty() {
		if (uncertaintyParameter1 != null) {
			uncertaintyParameter1.removePropertyChangeListener(this);
			uncertaintyParameter1 = null;
		}
		if (uncertaintyParameter2 != null) {
			uncertaintyParameter2.removePropertyChangeListener(this);
			uncertaintyParameter2 = null;
		}
		if (uncertaintyParameter3 != null) {
			uncertaintyParameter3.removePropertyChangeListener(this);
			uncertaintyParameter3 = null;
		}
	}

	private void setUncertaintyValues(
			UncertaintyDistributionType distributionType) {
		String formula = resultingAmount.getFormula();
		double value = resultingAmount.getValue();
		switch (distributionType) {
		case NORMAL:
			setUncertainty(1, formula, value);
			setUncertainty(2, "1", 1d);
			break;
		case LOG_NORMAL:
			setUncertainty(1, formula, value);
			setUncertainty(2, "1", 1d);
			break;
		case TRIANGLE:
			setUncertainty(1, formula, value);
			setUncertainty(2, formula, value);
			setUncertainty(3, formula, value);
			break;
		case UNIFORM:
			setUncertainty(1, formula, value);
			setUncertainty(2, formula, value);
			break;
		default:
			break;
		}
	}

	private void setUncertainty(int param, String formula, double value) {
		Expression e = new Expression(formula, value);
		e.addPropertyChangeListener(this);
		switch (param) {
		case 1:
			uncertaintyParameter1 = e;
			break;
		case 2:
			uncertaintyParameter2 = e;
			break;
		case 3:
			uncertaintyParameter3 = e;
			break;
		default:
			break;
		}
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the flow-field
	 * </p>
	 * 
	 * @param flow
	 *            <p style="margin-top: 0">
	 *            The flow of the exchange
	 *            </p>
	 */
	public void setFlow(final Flow flow) {
		support.firePropertyChange("flow", this.flow, this.flow = flow);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the flowPropertyFactor-field
	 * </p>
	 * 
	 * @param flowPropertyFactor
	 *            <p style="margin-top: 0">
	 *            The flow property of the exchange including the conversion
	 *            factor
	 *            </p>
	 */
	public void setFlowPropertyFactor(
			final FlowPropertyFactor flowPropertyFactor) {
		support.firePropertyChange("flowPropertyFactor",
				this.flowPropertyFactor,
				this.flowPropertyFactor = flowPropertyFactor);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the input-field
	 * </p>
	 * 
	 * @param input
	 *            <p style="margin-top: 0">
	 *            Indicates whether the exchange is an input or an output
	 *            </p>
	 */
	public void setInput(final boolean input) {
		support.firePropertyChange("input", this.input, this.input = input);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the unit-field
	 * </p>
	 * 
	 * @param unit
	 *            <p style="margin-top: 0">
	 *            The unit of the exchange
	 *            </p>
	 */
	public void setUnit(final Unit unit) {
		support.firePropertyChange("unit", this.unit, this.unit = unit);
	}

	public String getPedigreeUncertainty() {
		return pedigreeUncertainty;
	}

	public void setPedigreeUncertainty(String pedigreeUncertainty) {
		support.firePropertyChange("pedigreeUncertainty",
				this.pedigreeUncertainty,
				this.pedigreeUncertainty = pedigreeUncertainty);
	}

	public Double getBaseUncertainty() {
		return baseUncertainty;
	}

	public void setBaseUncertainty(Double baseUncertainty) {
		support.firePropertyChange("baseUncertainty", this.baseUncertainty,
				this.baseUncertainty = baseUncertainty);
	}

	public String getDefaultProviderId() {
		return defaultProviderId;
	}

	public void setDefaultProviderId(String defaultProviderId) {
		support.firePropertyChange("defaultProviderId", this.defaultProviderId,
				this.defaultProviderId = defaultProviderId);
	}

	@Override
	public String toString() {
		return "Exchange [flow=" + flow + ", input=" + input + ", ownerId="
				+ ownerId + ", resultingAmount=" + resultingAmount + ", unit="
				+ unit + "]";
	}

}
