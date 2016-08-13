package org.openlca.core.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/** A process link is a connection between two processes in a product system. */
@Embeddable
public class ProcessLink implements Cloneable {

	/**
	 * ID of the flow that is an output of the one process and an input of the
	 * of the other process.
	 */
	@Column(name = "f_flow")
	public long flowId;

	/**
	 * ID of the process that is an provider of a product (has a product output)
	 * or a waste treatment (has a waste input). The pair (providerId, flowId)
	 * is used to index the matrices in the calculation.
	 */
	@Column(name = "f_provider")
	public long providerId;

	@Column(name = "f_recipient")
	public long recipientId;

	@Override
	public ProcessLink clone() {
		ProcessLink clone = new ProcessLink();
		clone.flowId = flowId;
		clone.providerId = providerId;
		clone.recipientId = recipientId;
		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!Objects.equals(getClass(), obj.getClass()))
			return false;
		ProcessLink other = (ProcessLink) obj;
		return this.flowId == other.flowId
				&& this.providerId == other.providerId
				&& this.recipientId == other.recipientId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.flowId, this.providerId, this.recipientId);
	}

}
