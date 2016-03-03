package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_units")
public class Unit extends RootEntity {

	@Column(name = "conversion_factor")
	private double conversionFactor = 1d;

	@Column(name = "synonyms")
	private String synonyms;

	public double getConversionFactor() {
		return conversionFactor;
	}

	@Override
	public Unit clone() {
		Unit unit = new Unit();
		Util.cloneRootFields(this, unit);
		unit.setConversionFactor(getConversionFactor());
		unit.setSynonyms(getSynonyms());
		return unit;
	}

	public String getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(String synonyms) {
		this.synonyms = synonyms;
	}

	public void setConversionFactor(double conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	@Override
	public String toString() {
		return "Unit [id=" + getId() + ", name=" + getName() + "]";
	}

}
