package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_allocation_factors")
public class AllocationFactor extends AbstractEntity implements Cloneable {

	@Column(name = "product_id")
	private long productId;

	@Column(name = "value")
	private double value;

	@Override
	public AllocationFactor clone() {
		AllocationFactor clone = new AllocationFactor();
		clone.setProductId(getProductId());
		clone.setValue(getValue());
		return clone;
	}

	public long getProductId() {
		return productId;
	}

	public double getValue() {
		return value;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public void setValue(double value) {
		this.value = value;
	}

}
