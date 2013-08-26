package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_project_variants")
public class ProjectVariant extends AbstractEntity {

	@Column(name = "name")
	private String name;

	@OneToOne
	@JoinColumn(name = "f_product_system")
	private ProductSystem productSystem;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	private List<ParameterRedef> parameterRedefs = new ArrayList<>();

	@Override
	public ProjectVariant clone() {
		ProjectVariant clone = new ProjectVariant();
		clone.setName(getName());
		clone.setProductSystem(getProductSystem());
		for (ParameterRedef redef : getParameterRedefs())
			clone.getParameterRedefs().add(redef.clone());
		return clone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProductSystem getProductSystem() {
		return productSystem;
	}

	public void setProductSystem(ProductSystem productSystem) {
		this.productSystem = productSystem;
	}

	public List<ParameterRedef> getParameterRedefs() {
		return parameterRedefs;
	}
}
