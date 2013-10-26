package org.openlca.core.results;

import org.openlca.core.math.IMatrix;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.LongIndex;
import org.openlca.core.matrix.ProductIndex;

/**
 * The result type of a normal inventory calculation. The flow result vector is
 * the vector g in the matrix method. The impact assessment results are derived
 * by F * g where F is the matrix with the impact assessment factors. Impact
 * assessment results are optional (means they are null when the result was
 * calculated without an impact assessment method).
 * 
 * TODO: we could easily add flow contributions to impact categories via the
 * formula C = F * diag(g).
 * 
 */
public class InventoryResult {

	private ProductIndex productIndex;
	private FlowIndex flowIndex;
	private LongIndex impactIndex;
	private IMatrix flowResultVector;
	private IMatrix impactResultVector;
	private IMatrix scalingVector;

	// result generators
	private InventoryFlowResults flowResults;
	private InventoryImpactResults impactResults;

	public InventoryFlowResults getFlowResults() {
		if (flowResults == null)
			flowResults = new InventoryFlowResults(this);
		return flowResults;
	}

	public InventoryImpactResults getImpactResults() {
		if (impactResults == null)
			impactResults = new InventoryImpactResults(this);
		return impactResults;
	}

	public void setProductIndex(ProductIndex productIndex) {
		this.productIndex = productIndex;
	}

	public void setFlowIndex(FlowIndex flowIndex) {
		this.flowIndex = flowIndex;
	}

	public void setImpactIndex(LongIndex impactIndex) {
		this.impactIndex = impactIndex;
	}

	public void setFlowResultVector(IMatrix flowResultVector) {
		this.flowResultVector = flowResultVector;
	}

	public void setImpactResultVector(IMatrix impactResultVector) {
		this.impactResultVector = impactResultVector;
	}

	public void setScalingVector(IMatrix scalingVector) {
		this.scalingVector = scalingVector;
	}

	public ProductIndex getProductIndex() {
		return productIndex;
	}

	public FlowIndex getFlowIndex() {
		return flowIndex;
	}

	public LongIndex getImpactIndex() {
		return impactIndex;
	}

	public IMatrix getFlowResultVector() {
		return flowResultVector;
	}

	public IMatrix getImpactResultVector() {
		return impactResultVector;
	}

	public IMatrix getScalingVector() {
		return scalingVector;
	}

	public double getFlowResult(long flowId) {
		int idx = flowIndex.getIndex(flowId);
		if (idx < 0 || idx >= flowResultVector.getRowDimension())
			return 0;
		double val = flowResultVector.getEntry(idx, 0);
		return adoptFlowResult(val, flowId);
	}

	private double adoptFlowResult(double value, long flowId) {
		if (value == 0)
			return 0; // avoid -0 in the results
		boolean inputFlow = flowIndex.isInput(flowId);
		return inputFlow ? -value : value;
	}

	public boolean hasImpactResults() {
		return impactIndex != null && !impactIndex.isEmpty()
				&& impactResultVector != null
				&& impactResultVector.getRowDimension() > 0;
	}

	public double getImpactResult(long impactCategoryId) {
		if (!hasImpactResults())
			return 0;
		int idx = impactIndex.getIndex(impactCategoryId);
		if (idx < 0 || idx >= impactResultVector.getRowDimension())
			return 0;
		return impactResultVector.getEntry(idx, 0);
	}

}
