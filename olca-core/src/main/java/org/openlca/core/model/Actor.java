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
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.openlca.core.model.modelprovider.IModelComponent;

/**
 * <p style="margin-top: 0">
 * An actor can be a person or an organization (i.e. a software user, data
 * provider, author of some report, or the like). Actors are used to link to and
 * store contact information of certain model components.
 * </p>
 */
@Entity
@Table(name = "tbl_actors")
public class Actor extends AbstractEntity implements Copyable<Actor>, IModelComponent,
		IdentifyableByVersionAndUUID {

	@Column(name = "address")
	private String address;

	@Column(length = 36, name = "categoryid")
	private String categoryId;

	@Column(name = "city")
	private String city;

	@Column(name = "country")
	private String country;

	@Lob
	@Column(name = "description")
	private String description;

	@Column(name = "email")
	private String eMail;

	@Column(name = "name")
	private String name;

	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	@Column(name = "telefax")
	private String telefax;

	@Column(name = "telephone")
	private String telephone;

	@Column(name = "website")
	private String webSite;

	@Column(name = "zipcode")
	private String zipCode;

	/**
	 * <p style="margin-top: 0">
	 * Creates a new actor
	 * </p>
	 */
	public Actor() {
	}

	public Actor(final String id, final String name) {
		setId(id);
		this.name = name;
	}

	@Override
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the address-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The address of the actor
	 *         </p>
	 */
	public String getAddress() {
		return address;
	}

	@Override
	public String getCategoryId() {
		return categoryId;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the city-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The city of the actors address
	 *         </p>
	 */
	public String getCity() {
		return city;
	}

	@Override
	public Actor copy() {
		final Actor actor = new Actor(UUID.randomUUID().toString(), getName());
		actor.setAddress(getAddress());
		actor.setCategoryId(getCategoryId());
		actor.setCity(getCity());
		actor.setCountry(getCountry());
		actor.setDescription(getDescription());
		actor.setEMail(getEMail());
		actor.setTelefax(getTelefax());
		actor.setTelephone(getTelephone());
		actor.setWebSite(getWebSite());
		actor.setZipCode(getZipCode());
		return actor;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the country-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The country of the actors address
	 *         </p>
	 */
	public String getCountry() {
		return country;
	}

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the eMail-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The e-Mail address of the actor
	 *         </p>
	 */
	public String getEMail() {
		return eMail;
	}

	@Override
	public String getUUID() {
		return getId();
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the telefax-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The telefax number of the actor
	 *         </p>
	 */
	public String getTelefax() {
		return telefax;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the telephone-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The telephone number of the actor
	 *         </p>
	 */
	public String getTelephone() {
		return telephone;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the webSite-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The website of the actor
	 *         </p>
	 */
	public String getWebSite() {
		return webSite;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the zipCode-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The zip code of the actors address
	 *         </p>
	 */
	public String getZipCode() {
		return zipCode;
	}

	@Override
	public void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the address-field
	 * </p>
	 * 
	 * @param address
	 *            <p style="margin-top: 0">
	 *            The address of the actor
	 *            </p>
	 */
	public void setAddress(final String address) {
		support.firePropertyChange("address", this.address,
				this.address = address);
	}

	@Override
	public void setCategoryId(final String categoryId) {
		support.firePropertyChange("categoryId", this.categoryId,
				this.categoryId = categoryId);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the city-field
	 * </p>
	 * 
	 * @param city
	 *            <p style="margin-top: 0">
	 *            The city of the actors address
	 *            </p>
	 */
	public void setCity(final String city) {
		support.firePropertyChange("city", this.city, this.city = city);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the country-field
	 * </p>
	 * 
	 * @param country
	 *            <p style="margin-top: 0">
	 *            The country of the actors address
	 *            </p>
	 */
	public void setCountry(final String country) {
		support.firePropertyChange("country", this.country,
				this.country = country);
	}

	@Override
	public void setDescription(final String description) {
		support.firePropertyChange("description", this.description,
				this.description = description);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the eMail-field
	 * </p>
	 * 
	 * @param eMail
	 *            <p style="margin-top: 0">
	 *            The e-Mail address of the actor
	 *            </p>
	 */
	public void setEMail(final String eMail) {
		support.firePropertyChange("eMail", this.eMail, this.eMail = eMail);
	}

	@Override
	public void setName(final String name) {
		support.firePropertyChange("name", this.name, this.name = name);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the telefax-field
	 * </p>
	 * 
	 * @param telefax
	 *            <p style="margin-top: 0">
	 *            The telefax number of the actor
	 *            </p>
	 */
	public void setTelefax(final String telefax) {
		support.firePropertyChange("telefax", this.telefax,
				this.telefax = telefax);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the telephone-field
	 * </p>
	 * 
	 * @param telephone
	 *            <p style="margin-top: 0">
	 *            The telephone number of the actor
	 *            </p>
	 */
	public void setTelephone(final String telephone) {
		support.firePropertyChange("telephone", this.telephone,
				this.telephone = telephone);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the webSite-field
	 * </p>
	 * 
	 * @param webSite
	 *            <p style="margin-top: 0">
	 *            The website of the actor
	 *            </p>
	 */
	public void setWebSite(final String webSite) {
		support.firePropertyChange("webSite", this.webSite,
				this.webSite = webSite);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the zipCode-field
	 * </p>
	 * 
	 * @param zipCode
	 *            <p style="margin-top: 0">
	 *            The zip code of the actors address
	 *            </p>
	 */
	public void setZipCode(final String zipCode) {
		support.firePropertyChange("zipCode", this.zipCode,
				this.zipCode = zipCode);
	}

}
