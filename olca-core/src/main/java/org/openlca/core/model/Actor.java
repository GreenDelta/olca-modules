package org.openlca.core.model;

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

	@Override
	public Actor clone() {
		Actor clone = new Actor();
		Util.copyRootFields(this, clone);
		clone.address = address;
		clone.category = category;
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
