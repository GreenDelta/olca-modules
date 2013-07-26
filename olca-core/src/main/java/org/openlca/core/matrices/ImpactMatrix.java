package org.openlca.core.matrices;

import org.openlca.core.indices.FlowIndex;
import org.openlca.core.indices.LongIndex;
import org.openlca.core.math.IMatrix;

/**
 * A matrix with impact assessment factors where the flows are mapped to the
 * columns and the impact categories are mapped to the rows. The factors should
 * be negative in this matrix if the correspondig flow is an input flow.
 */
public class ImpactMatrix {

	private FlowIndex flowIndex;
	private LongIndex categoryIndex;
	private IMatrix values;

	public FlowIndex getFlowIndex() {
		return flowIndex;
	}

	public void setFlowIndex(FlowIndex flowIndex) {
		this.flowIndex = flowIndex;
	}

	public LongIndex getCategoryIndex() {
		return categoryIndex;
	}

	public void setCategoryIndex(LongIndex categoryIndex) {
		this.categoryIndex = categoryIndex;
	}

	public IMatrix getValues() {
		return values;
	}

	public void setValues(IMatrix values) {
		this.values = values;
	}

}
