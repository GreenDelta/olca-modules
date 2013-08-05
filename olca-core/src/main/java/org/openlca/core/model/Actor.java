package org.openlca.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * An actor can be a person or an organisation.
 */
@Entity
@Table(name = "tbl_actors")
public class Actor extends CategorizedEntity {

	@Column(name = "address")
	private String address;

	@Column(name = "city")
	private String city;

	@Column(name = "country")
	private String country;

	@Column(name = "email")
	private String email;

	@Column(name = "telefax")
	private String telefax;

	@Column(name = "telephone")
	private String telephone;

	@Column(name = "website")
	private String website;

	@Column(name = "zip_code")
	private String zipCode;

	@Override
	public Actor clone() {
		Actor actor = new Actor();
		actor.setRefId(UUID.randomUUID().toString());
		actor.setName(getName());
		actor.setAddress(getAddress());
		actor.setCategory(getCategory());
		actor.setCity(getCity());
		actor.setCountry(getCountry());
		actor.setDescription(getDescription());
		actor.setEmail(getEmail());
		actor.setTelefax(getTelefax());
		actor.setTelephone(getTelephone());
		actor.setWebsite(getWebsite());
		actor.setZipCode(getZipCode());
		return actor;
	}

	public String getAddress() {
		return address;
	}

	public String getCity() {
		return city;
	}

	public String getCountry() {
		return country;
	}

	public String getEmail() {
		return email;
	}

	public String getTelefax() {
		return telefax;
	}

	public String getTelephone() {
		return telephone;
	}

	public String getWebsite() {
		return website;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setTelefax(String telefax) {
		this.telefax = telefax;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

}
