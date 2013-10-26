package org.openlca.core.math;

import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.results.AnalysisResult;
import org.openlca.core.results.InventoryResult;

/**
 * Calculates the inventory result or analysis result.
 */
public interface InventorySolver {

	InventoryResult solve(InventoryMatrix matrix, IMatrixFactory factory);

	AnalysisResult analyse(InventoryMatrix matrix, IMatrixFactory factory);

}
