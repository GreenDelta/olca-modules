package org.openlca.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_units")
public class Unit extends RootEntity {

	@Column(name = "conversion_factor")
	public double conversionFactor = 1d;

	@Column(name = "synonyms")
	public String synonyms;

	/**
	 * Creates a new unit with the given name and a conversion factor of 1.
	 */
	public static Unit of(String name) {
		var unit = new Unit();
		unit.name = name;
		unit.refId = UUID.randomUUID().toString();
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
	public Unit clone() {
		var unit = new Unit();
		Util.copyRootFields(this, unit);
		unit.conversionFactor = conversionFactor;
		unit.synonyms = synonyms;
		return unit;
	}

	@Override
	public String toString() {
		return "Unit [id=" + id + ", name=" + name + "]";
	}

}
