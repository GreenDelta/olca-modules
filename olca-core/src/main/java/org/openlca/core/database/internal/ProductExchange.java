package org.openlca.core.database.internal;

import com.google.common.primitives.Longs;

/**
 * Internal representation of a product input or output. The ID is the real
 * exchange ID from the database.
 */
class ProductExchange {

	private long id;
	private long processId;
	private long flowId;
	private double amount;
	private Long defaultProviderId;

	/** Only valid for inputs. */
	public Long getDefaultProviderId() {
		return defaultProviderId;
	}

	/** Only valid for inputs. */
	public void setDefaultProviderId(Long defaultProviderId) {
		this.defaultProviderId = defaultProviderId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getProcessId() {
		return processId;
	}

	public void setProcessId(long processId) {
		this.processId = processId;
	}

	public long getFlowId() {
		return flowId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Override
	public int hashCode() {
		return Longs.hashCode(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductExchange other = (ProductExchange) obj;
		return this.id == other.id;
	}

}
