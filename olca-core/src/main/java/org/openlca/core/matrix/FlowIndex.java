package org.openlca.core.matrix;

import org.openlca.core.model.descriptors.FlowDescriptor;

import gnu.trove.map.hash.TLongByteHashMap;

/**
 * The row index $\mathit{Idx}_B$ of the intervention matrix $\mathbf{B}$. It
 * maps the (elementary) flows $\mathit{F}$ of the processes in the product
 * system to the $k$ rows of $\mathbf{B}$.
 *
 * $$\mathit{Idx}_B: \mathit{F} \mapsto [0 \dots k-1]$$
 */
public class FlowIndex extends DIndex<FlowDescriptor> {

	private TLongByteHashMap inputs = new TLongByteHashMap();

	public int putInput(FlowDescriptor flow) {
		if (flow == null)
			return -1;
		inputs.put(flow.id, (byte) 1);
		return put(flow);
	}

	public int putOutput(FlowDescriptor flow) {
		if (flow == null)
			return -1;
		inputs.put(flow.id, (byte) 0);
		return put(flow);
	}

	public boolean isInput(FlowDescriptor flow) {
		if (flow == null)
			return false;
		return isInput(flow.id);
	}

	public boolean isInput(long flowId) {
		byte input = inputs.get(flowId);
		return input == 1;
	}

}
