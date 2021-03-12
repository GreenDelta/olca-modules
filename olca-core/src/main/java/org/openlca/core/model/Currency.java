package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_currencies")
public class Currency extends CategorizedEntity {

	@Column(name = "code")
	public String code;

	@Column(name = "conversion_factor")
	public double conversionFactor;

	@OneToOne
	@JoinColumn(name = "f_reference_currency")
	public Currency referenceCurrency;

	public static Currency of(String name) {
		var currency = new Currency();
		Entities.init(currency, name);
		return currency;
	}

	@Override
	public Currency clone() {
		var clone = new Currency();
		Entities.copyFields(this, clone);
		clone.code = code;
		clone.conversionFactor = conversionFactor;
		clone.referenceCurrency = referenceCurrency;
		return clone;
	}
}
