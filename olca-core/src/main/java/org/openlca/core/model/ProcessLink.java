package org.openlca.core.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * A process link is a connection between a provider (which can be a process or
 * product system) and a process in a product system.
 */
@Embeddable
public class ProcessLink implements Cloneable {

	/**
	 * ID of the flow that is an output of the one process (or the reference flow of
	 * a product system) and an input of the other process.
	 */
	@Column(name = "f_flow")
	public long flowId;

	/**
	 * ID of the process or product system that is an provider of a product (has a
	 * product output) or a waste treatment (has a waste input). The pair
	 * (providerId, flowId) is used to index the matrices in the calculation.
	 */
	@Column(name = "f_provider")
	public long providerId;

	/**
	 * ID of the process that has a link to the provider (has a product input or
	 * waste output).
	 */
	@Column(name = "f_process")
	public long processId;

	/**
	 * ID of the product input or waste output of the process that is linked to a
	 * provider. Note that an exchange can be linked to only one provider in a
	 * product system but a provider can be linked to multiple exchanges. Also,
	 * there can be multiple exchanges in a process with the same flow that are
	 * linked to different providers.
	 */
	@Column(name = "f_exchange")
	public long exchangeId;

	/**
	 * When true, the provider of this link is a product system.
	 */
	@Column(name = "is_system_link")
	public boolean isSystemLink;

	@Override
	public ProcessLink clone() {
		var clone = new ProcessLink();
		clone.flowId = flowId;
		clone.providerId = providerId;
		clone.processId = processId;
		clone.exchangeId = exchangeId;
		clone.isSystemLink = isSystemLink;
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
				&& this.processId == other.processId
				&& this.exchangeId == other.exchangeId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				this.flowId,
				this.providerId,
				this.processId,
				this.exchangeId);
	}

}
