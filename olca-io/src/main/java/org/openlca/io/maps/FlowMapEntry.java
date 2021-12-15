package org.openlca.io.maps;

import org.openlca.core.model.Copyable;

/**
 * Describes a single mapping between two flows.
 *
 * @param sourceFlow the source/original flow of the mapping
 * @param targetFlow the corresponding target flow of the mapping
 * @param factor     the conversion factor which is applied to the amounts of
 *                   the source flow to convert them into the corresponding
 *                   amounts of the target flow (in the respective flow
 *                   properties and units); defaults to 1.0
 */
public record FlowMapEntry(
	FlowRef sourceFlow,
	FlowRef targetFlow,
	double factor
) implements Copyable<FlowMapEntry> {

	public FlowMapEntry(FlowRef sourceFlow, FlowRef targetFlow) {
		this(sourceFlow, targetFlow, 1);
	}

	/**
	 * Swap the source and target flow reference in this entry and inverts the
	 * conversion factor.
	 */
	public FlowMapEntry swap() {
		var inv = factor == 0 || factor == 1
			? factor
			: 1 / factor;
		return new FlowMapEntry(targetFlow, sourceFlow, inv);
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
