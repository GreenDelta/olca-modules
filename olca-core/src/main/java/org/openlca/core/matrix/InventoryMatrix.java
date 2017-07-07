package org.openlca.core.matrix;

import org.openlca.core.matrix.format.IMatrix;

/**
 * The inventory of a product system where the values are mapped to real
 * matrices.
 */
public class InventoryMatrix {

	public IMatrix technologyMatrix;
	public IMatrix interventionMatrix;
	public TechIndex productIndex;
	public FlowIndex flowIndex;

	/**
	 * Indicates that there are no elementary flows or technology flows in the
	 * matrix and no result can be calculated.
	 */
	public boolean isEmpty() {
		return flowIndex == null || interventionMatrix == null
				|| flowIndex.size() == 0 || productIndex == null
				|| productIndex.size() == 0 || technologyMatrix == null;
	}

}
