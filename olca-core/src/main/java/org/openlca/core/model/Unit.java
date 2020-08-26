package org.openlca.core.model;

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

	@Override
	public Unit clone() {
		Unit unit = new Unit();
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
