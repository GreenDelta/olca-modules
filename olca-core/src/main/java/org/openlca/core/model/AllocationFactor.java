package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_allocation_factors")
public class AllocationFactor extends AbstractEntity implements Cloneable {

	@Column(name = "f_product")
	private long productId;

	@Column(name = "allocation_type")
	@Enumerated(EnumType.STRING)
	private AllocationMethod allocationType;

	@Column(name = "value")
	private double value;

	@OneToOne
	@JoinColumn(name = "f_exchange")
	private Exchange exchange;

	@Override
	public AllocationFactor clone() {
		AllocationFactor clone = new AllocationFactor();
		clone.setProductId(getProductId());
		clone.setAllocationType(getAllocationType());
		clone.setExchange(getExchange());
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

	public AllocationMethod getAllocationType() {
		return allocationType;
	}

	public void setAllocationType(AllocationMethod allocationType) {
		this.allocationType = allocationType;
	}

	public Exchange getExchange() {
		return exchange;
	}

	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}

}
