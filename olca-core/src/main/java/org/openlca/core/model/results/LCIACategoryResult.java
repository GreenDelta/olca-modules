/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.model.results;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.openlca.core.model.AbstractEntity;

/**
 * An LCIA category result
 * 
 */
@Entity
@Table(name = "tbl_lciacategoryresults")
public class LCIACategoryResult extends AbstractEntity {

	@Column(name = "category")
	private String category;

	@Column(name = "normalizationfactor")
	private double normalizationFactor;

	@Column(name = "standarddeviation")
	private double standardDeviation;

	@Column(name = "unit")
	private String unit;

	@Column(name = "value")
	private double value;

	@Column(name = "weightingfactor")
	private double weightingFactor;

	@Column(name = "weightingunit")
	private String weightingUnit;

	public void setCategory(String category) {
		this.category = category;
	}

	public void setNormalizationFactor(double normalizationFactor) {
		this.normalizationFactor = normalizationFactor;
	}

	public void setStandardDeviation(double standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public void setWeightingFactor(double weightingFactor) {
		this.weightingFactor = weightingFactor;
	}

	public void setWeightingUnit(String weightingUnit) {
		this.weightingUnit = weightingUnit;
	}

	public String getCategory() {
		return category;
	}

	public double getNormalizedValue() {
		return value / normalizationFactor;
	}

	public double getStandardDeviation() {
		return standardDeviation;
	}

	public String getUnit() {
		return unit;
	}

	public double getValue() {
		return value;
	}

	public double getWeightedValue() {
		return value / normalizationFactor * weightingFactor;
	}

	public String getWeightingUnit() {
		return weightingUnit;
	}
}
