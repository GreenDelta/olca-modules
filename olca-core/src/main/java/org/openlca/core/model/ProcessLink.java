package org.openlca.core.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * A process link is a link between a providing process and a receiving process.
 * The link is realized with a providing exchange and a receiving exchange,
 * which must have the same flow
 */
@Embeddable
public class ProcessLink implements Cloneable {

	@Column(name = "f_flow")
	private long flowId;

	@Column(name = "f_provider")
	private long providerId;

	@Column(name = "f_recipient")
	private long recipientId;

	@Override
	public ProcessLink clone() {
		ProcessLink clone = new ProcessLink();
		clone.setFlowId(getFlowId());
		clone.setProviderId(getProviderId());
		clone.setRecipientId(getRecipientId());
		return clone;
	}

	public long getFlowId() {
		return flowId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
	}

	public long getProviderId() {
		return providerId;
	}

	public void setProviderId(long providerId) {
		this.providerId = providerId;
	}

	public long getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(long recipientId) {
		this.recipientId = recipientId;
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
