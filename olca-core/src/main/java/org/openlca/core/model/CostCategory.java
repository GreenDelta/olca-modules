package org.openlca.core.model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_cost_categories")
public class CostCategory extends CategorizedEntity {

	@Override
	public CostCategory clone() {
		CostCategory clone = new CostCategory();
		Util.cloneRootFields(this, clone);
		clone.setCategory(getCategory());
		return clone;
	}
}
