package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_units")
public class Unit extends RefEntity {

	@Column(name = "conversion_factor")
	public double conversionFactor = 1d;

	@Column(name = "synonyms")
	public String synonyms;

	/**
	 * Creates a new unit with the given name and a conversion factor of 1.
	 */
	public static Unit of(String name) {
		var unit = new Unit();
		Entities.init(unit, name);
		unit.conversionFactor = 1.0;
		return unit;
	}

	/**
	 * Creates a new unit with the given name and conversion factor.
	 */
	public static Unit of(String name, double conversionFactor) {
		var unit = of(name);
		unit.conversionFactor = conversionFactor;
		return unit;
	}

	@Override
	public Unit copy() {
		var unit = new Unit();
		Entities.copyRefFields(this, unit);
		unit.conversionFactor = conversionFactor;
		unit.synonyms = synonyms;
		return unit;
	}

	@Override
	public String toString() {
		return "Unit [id=" + id + ", name=" + name + "]";
	}

}
