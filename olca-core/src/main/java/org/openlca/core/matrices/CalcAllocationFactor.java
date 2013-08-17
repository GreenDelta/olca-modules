package org.openlca.core.matrices;

import org.openlca.core.model.AllocationMethod;

public class CalcAllocationFactor {

	private long processId;
	private long productId;
	private AllocationMethod method;
	private double value;
	private Long exchangeId;

	public long getProcessId() {
		return processId;
	}

	public void setProcessId(long processId) {
		this.processId = processId;
	}

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public AllocationMethod getMethod() {
		return method;
	}

	public void setMethod(AllocationMethod method) {
		this.method = method;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public Long getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(Long exchangeId) {
		this.exchangeId = exchangeId;
	}

}
