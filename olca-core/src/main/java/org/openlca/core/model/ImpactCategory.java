package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_impact_categories")
public class ImpactCategory extends RootEntity {

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_impact_category")
	public final List<ImpactFactor> impactFactors = new ArrayList<>();

	@Column(name = "reference_unit")
	public String referenceUnit;

	@Override
	public ImpactCategory clone() {
		ImpactCategory clone = new ImpactCategory();
		Util.cloneRootFields(this, clone);
		clone.referenceUnit = referenceUnit;
		for (ImpactFactor f : impactFactors)
			clone.impactFactors.add(f.clone());
		return clone;
	}

}
