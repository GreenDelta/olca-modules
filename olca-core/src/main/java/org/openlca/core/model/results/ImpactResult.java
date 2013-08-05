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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.openlca.core.model.CategorizedEntity;

/**
 * This class represents a calculation result for a specific product system
 */
@Entity
@Table(name = "tbl_impact_results")
public class ImpactResult extends CategorizedEntity {

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_lciaresult")
	private List<ImpactCategoryResult> lciaCategoryResults = new ArrayList<>();

	@Column(name = "lcia_method")
	private String lciaMethod;

	@Column(name = "nw_set")
	private String normalizationWeightingSet;

	@Column(name = "product")
	private String product;

	@Column(name = "product_system")
	private String productSystem;

	@Column(name = "target_amount")
	private double targetAmount;

	@Column(name = "unit")
	private String unit;

	@Column(name = "weighting_unit")
	private String weightingUnit;

	public List<ImpactCategoryResult> getLCIACategoryResults() {
		return lciaCategoryResults;
	}

	public String getLCIAMethod() {
		return lciaMethod;
	}

	public String getNormalizationWeightingSet() {
		return normalizationWeightingSet;
	}

	public String getProduct() {
		return product;
	}

	public String getProductSystem() {
		return productSystem;
	}

	public double getTargetAmount() {
		return targetAmount;
	}

	public String getUnit() {
		return unit;
	}

	public String getWeightingUnit() {
		return weightingUnit;
	}

	public void setLciaCategoryResults(
			List<ImpactCategoryResult> lciaCategoryResults) {
		this.lciaCategoryResults = lciaCategoryResults;
	}

	public void setLciaMethod(String lciaMethod) {
		this.lciaMethod = lciaMethod;
		updateName();
	}

	public void setNormalizationWeightingSet(String normalizationWeightingSet) {
		this.normalizationWeightingSet = normalizationWeightingSet;
		updateName();
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public void setProductSystem(String productSystem) {
		this.productSystem = productSystem;
		updateName();
	}

	public void setTargetAmount(double targetAmount) {
		this.targetAmount = targetAmount;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public void setWeightingUnit(String weightingUnit) {
		this.weightingUnit = weightingUnit;
	}

	private void updateName() {
		String name = productSystem;
		if (lciaMethod != null)
			name += (" - " + lciaMethod);
		if (normalizationWeightingSet != null)
			name += normalizationWeightingSet;
		setName(name);
	}

	@Override
	public ImpactResult clone() {
		ImpactResult clone = new ImpactResult();
		clone.setCategory(getCategory());
		clone.setDescription(getDescription());
		clone.setRefId(UUID.randomUUID().toString());
		// TODO: not yet implemented
		return clone;
	}

}
