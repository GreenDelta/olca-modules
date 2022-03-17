package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * An actor can be a person or an organisation.
 */
@Entity
@Table(name = "tbl_actors")
public class Actor extends RootEntity {

	@Column(name = "address")
	public String address;

	@Column(name = "city")
	public String city;

	@Column(name = "country")
	public String country;

	@Column(name = "email")
	public String email;

	@Column(name = "telefax")
	public String telefax;

	@Column(name = "telephone")
	public String telephone;

	@Column(name = "website")
	public String website;

	@Column(name = "zip_code")
	public String zipCode;

	public static Actor of(String name) {
		var actor = new Actor();
		Entities.init(actor, name);
		return actor;
	}

	@Override
	public Actor copy() {
		var clone = new Actor();
		Entities.copyFields(this, clone);
		clone.address = address;
		clone.city = city;
		clone.country = country;
		clone.email = email;
		clone.telefax = telefax;
		clone.telephone = telephone;
		clone.website = website;
		clone.zipCode = zipCode;
		return clone;
	}
}
