package org.openlca.io.maps;

import org.openlca.core.model.Copyable;

/**
 * Describes a single mapping between two flows.
 */
public class FlowMapEntry implements Copyable<FlowMapEntry> {

	private FlowRef sourceFlow;
	private FlowRef targetFlow;
	private double factor;

	/**
	 * Creates a new mapping entry with a factor of {@code 1}.
	 */
	public FlowMapEntry(FlowRef sourceFlow, FlowRef targetFlow) {
		this(sourceFlow, targetFlow, 1);
	}

	/**
	 * Creates a new mapping entry.
	 *
	 * @param sourceFlow the source/original flow of the mapping
	 * @param targetFlow the corresponding target flow of the mapping
	 * @param factor     the conversion factor which is applied to the amounts of
	 *                   the source flow to convert them into the corresponding
	 *                   amounts of the target flow (in the respective flow
	 *                   properties and units); defaults to 1.0
	 */
	public FlowMapEntry(FlowRef sourceFlow, FlowRef targetFlow, double factor) {
		this.sourceFlow = sourceFlow;
		this.targetFlow = targetFlow;
		this.factor = factor;
	}

	public FlowMapEntry sourceFlow(FlowRef sourceFlow) {
		this.sourceFlow = sourceFlow;
		return this;
	}

	public FlowRef sourceFlow() {
		return sourceFlow;
	}

	public FlowMapEntry targetFlow(FlowRef targetFlow) {
		this.targetFlow = targetFlow;
		return this;
	}

	public FlowRef targetFlow() {
		return targetFlow;
	}

	public FlowMapEntry factor(double factor) {
		this.factor = factor;
		return this;
	}

	public double factor() {
		return factor;
	}

	/**
	 * Swap the source and target flow reference in this entry and inverts the
	 * conversion factor.
	 */
	public void swap() {
		if (factor != 0 && factor != 1) {
			factor = 1 / factor;
		}
		var s = sourceFlow;
		sourceFlow = targetFlow;
		targetFlow = s;
	}

	public String sourceFlowId() {
		if (sourceFlow == null || sourceFlow.flow == null)
			return null;
		return sourceFlow.flow.refId;
	}

	public String targetFlowId() {
		if (targetFlow == null || targetFlow.flow == null)
			return null;
		return targetFlow.flow.refId;
	}

	@Override
	public FlowMapEntry copy() {
		var sourceCopy = sourceFlow != null
			? sourceFlow.copy()
			: null;
		var targetCopy = targetFlow != null
			? targetFlow.copy()
			: null;
		return new FlowMapEntry(sourceCopy, targetCopy, factor);
	}
}
