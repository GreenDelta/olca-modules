package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_cost_categories")
public class CostCategory extends AbstractEntity {

	@Column(name = "name")
	private String name;

	@Column(name = "fix")
	private boolean fix;

	@Lob
	@Column(name = "description")
	private String description;

	@Override
	public CostCategory clone() {
		CostCategory clone = new CostCategory();
		clone.setFix(isFix());
		clone.setName(getName());
		clone.setDescription(getDescription());
		return clone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isFix() {
		return fix;
	}

	public void setFix(boolean fix) {
		this.fix = fix;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CostCategory))
			return false;
		CostCategory in = (CostCategory) obj;
		if ((in.name != null && in.name.equals(name)) || name == null)
			return in.fix == fix;
		return false;
	}

}
