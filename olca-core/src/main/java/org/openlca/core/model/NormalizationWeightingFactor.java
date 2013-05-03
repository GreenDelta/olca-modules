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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Has a normalization and a weighting factor. Normalization factors are hold in
 * a {@link NormalizationWeightingSet} of an LCIA method
 * 
 * @author Sebastian Greve
 * 
 */
@Entity
@Table(name = "tbl_normalizationweightingfactors")
public class NormalizationWeightingFactor extends AbstractEntity {

	@Column(name = "f_lciacategory")
	private String impactCategoryId;

	@Column(name = "normalizationfactor")
	private Double normalizationFactor;

	@Column(name = "weightingfactor")
	private Double weightingFactor;

	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	public Double getNormalizationFactor() {
		return normalizationFactor;
	}

	public Double getWeightingFactor() {
		return weightingFactor;
	}

	public void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	public void setNormalizationFactor(Double normalizationFactor) {
		support.firePropertyChange("normalizationFactor",
				this.normalizationFactor,
				this.normalizationFactor = normalizationFactor);
	}

	public void setWeightingFactor(Double weightingFactor) {
		support.firePropertyChange("weightingFactor", this.weightingFactor,
				this.weightingFactor = weightingFactor);
	}

	public void setImpactCategoryId(String impactCategoryId) {
		support.firePropertyChange("impactCategoryId", this.impactCategoryId,
				this.impactCategoryId = impactCategoryId);
	}

	public String getImpactCategoryId() {
		return impactCategoryId;
	}

}
