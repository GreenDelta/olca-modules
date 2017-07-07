package org.openlca.core.matrix;

import org.openlca.core.matrix.format.IMatrix;

public class ImpactMatrix {

	public LongIndex categoryIndex;
	public FlowIndex flowIndex;
	public IMatrix factorMatrix;

	public boolean isEmpty() {
		return flowIndex == null || flowIndex.size() == 0
				|| categoryIndex == null || categoryIndex.size() == 0
				|| factorMatrix == null;
	}

}
