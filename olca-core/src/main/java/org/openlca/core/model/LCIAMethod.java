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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * An impact assessment method.
 */
@Entity
@Table(name = "tbl_lciamethods")
public class LCIAMethod extends RootEntity implements IParameterisable {

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "f_lcia_method")
	private final List<LCIACategory> lciaCategories = new ArrayList<>();

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "f_lcia_method")
	private final List<NormalizationWeightingSet> normalizationWeightingSets = new ArrayList<>();

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	private final List<Parameter> parameters = new ArrayList<>();

	@Override
	public LCIAMethod clone() {
		LCIAMethod lciaMethod = new LCIAMethod();
		lciaMethod.setId(UUID.randomUUID().toString());
		lciaMethod.setName(getName());
		lciaMethod.setCategoryId(getCategoryId());
		lciaMethod.setDescription(getDescription());
		for (LCIACategory lciaCategory : getLCIACategories()) {
			lciaMethod.getLCIACategories().add(lciaCategory.clone());
		}
		// TODO: clone parameters and nw-sets!
		return lciaMethod;
	}

	public List<LCIACategory> getLCIACategories() {
		return lciaCategories;
	}

	public List<NormalizationWeightingSet> getNormalizationWeightingSets() {
		return normalizationWeightingSets;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

}