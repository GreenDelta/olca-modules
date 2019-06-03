package org.openlca.io.maps;

/**
 * A FlowMapEntry describes a single mapping between two flows.
 */
public class FlowMapEntry {

	/** Describes a flow of the source system of a conversion. */
	public FlowRef sourceFlow;

	/** Describes the corresponding flow of the target system. */
	public FlowRef targetFlow;

	/**
	 * An optional conversion factor which is applied to the amounts of the
	 * source flow to convert them into the corresponding amounts of the target
	 * flow (in the respective flow properties and units); defaults to 1.0
	 */
	public double factor = 1.0;

	/**
	 * Describes a synchronization result of this flow mapping with a database.
	 */
	public Status status;

	/**
	 * Swap the source and target flow reference in this entry and inverts the
	 * conversion factor.
	 */
	public void swap() {
		FlowRef s = sourceFlow;
		sourceFlow = targetFlow;
		targetFlow = s;
		if (factor != 0 && factor != 1.0) {
			factor = 1 / factor;
		}
	}

	public String sourceFlowID() {
		if (sourceFlow == null || sourceFlow.flow == null)
			return null;
		return sourceFlow.flow.refId;
	}

	public String targetFlowID() {
		if (targetFlow == null || targetFlow.flow == null)
			return null;
		return targetFlow.flow.refId;
	}

	@Override
	public FlowMapEntry clone() {
		FlowMapEntry clone = new FlowMapEntry();
		clone.factor = factor;
		if (sourceFlow != null) {
			clone.sourceFlow = sourceFlow.clone();
		}
		if (targetFlow != null) {
			clone.targetFlow = targetFlow.clone();
		}
		if (status != null) {
			clone.status = status.clone();
		}
		return clone;
	}
}
