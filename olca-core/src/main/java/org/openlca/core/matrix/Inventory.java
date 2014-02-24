package org.openlca.core.matrix;

import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IMatrixFactory;
import org.openlca.core.model.AllocationMethod;
import org.openlca.expressions.FormulaInterpreter;

/**
 * Contains all the information of the inventory of a complete product system.
 */
public class Inventory {

	private ProductIndex productIndex;
	private FlowIndex flowIndex;
	private ExchangeMatrix technologyMatrix;
	private ExchangeMatrix interventionMatrix;
	private AllocationMethod allocationMethod;

	public boolean isEmpty() {
		return productIndex == null || productIndex.size() == 0
				|| flowIndex == null || flowIndex.isEmpty()
				|| technologyMatrix == null || technologyMatrix.isEmpty()
				|| interventionMatrix == null || interventionMatrix.isEmpty();
	}

	public AllocationMethod getAllocationMethod() {
		return allocationMethod;
	}

	void setAllocationMethod(AllocationMethod allocationMethod) {
		this.allocationMethod = allocationMethod;
	}

	public ProductIndex getProductIndex() {
		return productIndex;
	}

	void setProductIndex(ProductIndex productIndex) {
		this.productIndex = productIndex;
	}

	public FlowIndex getFlowIndex() {
		return flowIndex;
	}

	void setFlowIndex(FlowIndex flowIndex) {
		this.flowIndex = flowIndex;
	}

	public ExchangeMatrix getTechnologyMatrix() {
		return technologyMatrix;
	}

	void setTechnologyMatrix(ExchangeMatrix technologyMatrix) {
		this.technologyMatrix = technologyMatrix;
	}

	public ExchangeMatrix getInterventionMatrix() {
		return interventionMatrix;
	}

	void setInterventionMatrix(ExchangeMatrix interventionMatrix) {
		this.interventionMatrix = interventionMatrix;
	}

	public InventoryMatrix createMatrix(IMatrixFactory<?> factory) {
		return createMatrix(factory, null);
	}

	public InventoryMatrix createMatrix(IMatrixFactory<?> factory,
			FormulaInterpreter interpreter) {
		evalFormulas(interpreter);
		InventoryMatrix matrix = new InventoryMatrix();
		matrix.setFlowIndex(flowIndex);
		matrix.setProductIndex(productIndex);
		IMatrix enviMatrix = interventionMatrix.createRealMatrix(factory);
		matrix.setInterventionMatrix(enviMatrix);
		IMatrix techMatrix = technologyMatrix.createRealMatrix(factory);
		matrix.setTechnologyMatrix(techMatrix);
		return matrix;
	}

	/**
	 * Re-evaluates the parameters and formulas in the inventory (because the
	 * may changed), generates new values for the entries that have an
	 * uncertainty distribution and set these values to the entries of the given
	 * matrix. The given matrix and this inventory have to match exactly in size
	 * (so normally you first call createMatrix and than simulate).
	 */
	public void simulate(InventoryMatrix matrix, FormulaInterpreter interpreter) {
		evalFormulas(interpreter);
		if (technologyMatrix != null)
			technologyMatrix.simulate(matrix.getTechnologyMatrix());
		if (interventionMatrix != null)
			interventionMatrix.simulate(matrix.getInterventionMatrix());
	}

	/**
	 * Evaluates the formulas in the exchange matrices of this inventory using
	 * the formula interpreter that is bound to this inventory. Does nothing if
	 * there is no interpreter set or if the exchange matrices are NULL.
	 */
	void evalFormulas(FormulaInterpreter interpreter) {
		if (interpreter == null)
			return;
		if (technologyMatrix != null)
			technologyMatrix.eval(interpreter);
		if (interventionMatrix != null)
			interventionMatrix.eval(interpreter);
	}

}
