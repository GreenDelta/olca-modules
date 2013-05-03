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
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.openlca.core.model.modelprovider.IParameterizedComponent;

/**
 * <p style="margin-top: 0">
 * An LCIA Method holds LCIA categories which specify LCIA factors for different
 * flows
 * </p>
 */
@Entity
@Table(name = "tbl_lciamethods")
public class LCIAMethod extends AbstractEntity implements Copyable<LCIAMethod>,
		PropertyChangeListener, IParameterizedComponent,
		IdentifyableByVersionAndUUID {

	@Column(length = 36, name = "categoryid")
	private String categoryId;

	@Lob
	@Column(name = "description")
	private String description;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "f_lciamethod")
	private final List<LCIACategory> lciaCategories = new ArrayList<>();

	@Column(name = "name")
	private String name;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "f_lciamethod")
	private final List<NormalizationWeightingSet> normalizationWeightingSets = new ArrayList<>();

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	private final List<Parameter> parameters = new ArrayList<>();

	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	public LCIAMethod() {
	}

	public LCIAMethod(final String id, final String name) {
		setId(id);
		this.name = name;
	}

	/**
	 * Initializes the property change listener after object is loaded from
	 * database
	 */
	@PostLoad
	protected void postLoad() {
		for (final LCIACategory category : getLCIACategories()) {
			category.addPropertyChangeListener(this);
		}
		for (final NormalizationWeightingSet nws : getNormalizationWeightingSets()) {
			nws.addPropertyChangeListener(this);
		}

	}

	/**
	 * <p style="margin-top: 0">
	 * Adds an LCIA category to the LCIA method
	 * 
	 * @param lciaCategory
	 *            The LCIA category to be added
	 *            </p>
	 */
	// TODO: check if we really need to add normalization and weighting factors
	// here
	public void add(final LCIACategory lciaCategory) {
		if (lciaCategory == null)
			return;
		lciaCategories.add(lciaCategory);
		support.firePropertyChange("lciaCategories", null, lciaCategory);
		lciaCategory.addPropertyChangeListener(this);
		for (NormalizationWeightingSet set : getNormalizationWeightingSets()) {
			NormalizationWeightingFactor fac = new NormalizationWeightingFactor();
			fac.setId(UUID.randomUUID().toString());
			fac.setImpactCategoryId(lciaCategory.getId());
			set.add(fac);
		}
	}

	/**
	 * Adds a normalization/weighting set
	 * 
	 * @param normalizationWeightingSet
	 *            The set to add
	 */
	public void add(final NormalizationWeightingSet normalizationWeightingSet) {
		if (normalizationWeightingSet != null) {
			normalizationWeightingSets.add(normalizationWeightingSet);
			normalizationWeightingSet.addPropertyChangeListener(this);
		}
		support.firePropertyChange("normalizationWeightingSets", null,
				normalizationWeightingSet);
	}

	@Override
	public void add(final Parameter parameter) {
		if (parameter != null) {
			parameters.add(parameter);
			support.firePropertyChange("parameters", null, parameter);
			parameter.addPropertyChangeListener(this);
		}
	}

	@Override
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	@Override
	public String getCategoryId() {
		return categoryId;
	}

	@Override
	public LCIAMethod copy() {
		final LCIAMethod lciaMethod = new LCIAMethod(UUID.randomUUID()
				.toString(), getName());
		lciaMethod.setCategoryId(getCategoryId());
		lciaMethod.setDescription(getDescription());
		for (final LCIACategory lciaCategory : getLCIACategories()) {
			lciaMethod.add(lciaCategory.copy());
		}
		return lciaMethod;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getUUID() {
		return getId();
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the LCIA categories
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The LCIA categories of the LCIA method
	 *         </p>
	 */
	public LCIACategory[] getLCIACategories() {
		return lciaCategories.toArray(new LCIACategory[lciaCategories.size()]);
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Getter of the normalization/weighting sets
	 * 
	 * @return The normalization/weighting sets of the LCIA method
	 */
	public NormalizationWeightingSet[] getNormalizationWeightingSets() {
		return normalizationWeightingSets
				.toArray(new NormalizationWeightingSet[normalizationWeightingSets
						.size()]);
	}

	@Override
	public Parameter[] getParameters() {
		return parameters.toArray(new Parameter[parameters.size()]);
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		support.firePropertyChange(evt);
	}

	/**
	 * <p style="margin-top: 0">
	 * Removes an LCIA category from the LCIA method
	 * 
	 * @param lciaCategory
	 *            The LCIA category to be removed
	 *            </p>
	 */
	public void remove(LCIACategory lciaCategory) {
		if (lciaCategory == null)
			return;
		lciaCategory.removePropertyChangeListener(this);
		lciaCategories.remove(lciaCategory);
		for (NormalizationWeightingSet set : getNormalizationWeightingSets()) {
			NormalizationWeightingFactor[] factors = set
					.getNormalizationWeightingFactors();
			for (NormalizationWeightingFactor factor : factors) {
				if (factor.getImpactCategoryId().equals(lciaCategory.getId())) {
					set.remove(factor);
					break;
				}
			}
		}
		support.firePropertyChange("lciaCategories", lciaCategory, null);
	}

	/**
	 * Removes a normalization/weighting set
	 * 
	 * @param normalizationWeightingSet
	 *            The set to remove
	 */
	public void remove(final NormalizationWeightingSet normalizationWeightingSet) {
		if (normalizationWeightingSet != null) {
			normalizationWeightingSets.remove(normalizationWeightingSet);
			normalizationWeightingSet.removePropertyChangeListener(this);
		}
		support.firePropertyChange("normalizationWeightingSets",
				normalizationWeightingSet, null);
	}

	@Override
	public void remove(final Parameter parameter) {
		if (parameter != null) {
			parameter.removePropertyChangeListener(this);
			parameters.remove(parameter);
		}
		support.firePropertyChange("parameters", parameter, null);
	}

	@Override
	public void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	@Override
	public void setCategoryId(final String categoryId) {
		support.firePropertyChange("categoryId", this.categoryId,
				this.categoryId = categoryId);
	}

	@Override
	public void setDescription(final String description) {
		support.firePropertyChange("description", this.description,
				this.description = description);
	}

	@Override
	public void setName(final String name) {
		support.firePropertyChange("name", this.name, this.name = name);
	}

}
