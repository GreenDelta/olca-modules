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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <p style="margin-top: 0">
 * An unit of measurement (i.e. kg)
 * </p>
 */
@Entity
@Table(name = "tbl_units")
public class Unit extends RootEntity {

	@Column(name = "conversion_factor")
	private double conversionFactor = 1d;

	@Column(name = "synonyms")
	private String synonyms;

	public double getConversionFactor() {
		return conversionFactor;
	}

	@Override
	public Unit clone() {
		final Unit unit = new Unit();
		unit.setName(getName());
		unit.setConversionFactor(getConversionFactor());
		unit.setDescription(getDescription());
		unit.setSynonyms(getSynonyms());
		return unit;
	}

	public String getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(String synonyms) {
		this.synonyms = synonyms;
	}

	public void setConversionFactor(double conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

}
