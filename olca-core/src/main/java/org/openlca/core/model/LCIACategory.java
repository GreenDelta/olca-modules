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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * <p style="margin-top: 0">
 * An LCIA category holds a set of LCIA factors which define for specific flows
 * how many they contribute to a specific LCIA method
 * </p>
 */
@Entity
@Table(name = "tbl_lciacategories")
public class LCIACategory extends AbstractEntity implements Cloneable {

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "f_impact_category")
	private final List<LCIAFactor> lciaFactors = new ArrayList<>();

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "reference_unit")
	private String referenceUnit;

	/**
	 * Returns the converted LCIA factor value for the given flow
	 * 
	 * @param flow
	 *            The flow the LCIA factor is requested for
	 * @return The converted LCIA factor value for the given flow
	 */
	public double getConvertedLCIAFactor(final Flow flow) {
		LCIAFactor factor = null;
		int i = 0;
		while (factor == null && i < lciaFactors.size()) {
			final LCIAFactor actual = lciaFactors.get(i);
			if (actual.getFlow().getId().equals(flow.getId())) {
				factor = actual;
			} else {
				i++;
			}
		}
		return factor == null ? 0 : factor.getConvertedValue();
	}

	@Override
	public LCIACategory clone() {
		final LCIACategory lciaCategory = new LCIACategory();
		lciaCategory.setId(UUID.randomUUID().toString());
		lciaCategory.setDescription(getDescription());
		lciaCategory.setName(getName());
		lciaCategory.setReferenceUnit(getReferenceUnit());
		for (LCIAFactor lciaFactor : getLCIAFactors()) {
			lciaCategory.getLCIAFactors().add(lciaFactor.clone());
		}
		return lciaCategory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReferenceUnit() {
		return referenceUnit;
	}

	public void setReferenceUnit(String referenceUnit) {
		this.referenceUnit = referenceUnit;
	}

	public List<LCIAFactor> getLCIAFactors() {
		return lciaFactors;
	}

}
