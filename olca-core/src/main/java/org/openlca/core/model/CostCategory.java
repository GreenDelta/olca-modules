package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_cost_categories")
public class CostCategory extends RootEntity {

	@Column(name = "fix")
	public boolean fix;

	@Override
	public CostCategory clone() {
		CostCategory clone = new CostCategory();
		Util.cloneRootFields(this, clone);
		clone.fix = fix;
		return clone;
	}
}
