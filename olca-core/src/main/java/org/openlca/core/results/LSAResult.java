package org.openlca.core.results;

import org.openlca.core.math.IMatrix;

public class LSAResult extends ContributionResult {
	private IMatrix[] rscA;
	private IMatrix[] rscB;
	public IMatrix getRscA(int impact) {
		return rscA[impact];
	}

	public void setRscA(IMatrix[] rscA) {
		this.rscA = rscA;
	}

	public IMatrix getRscB(int impact) {
		return rscB[impact];
	}

	public void setRscB(IMatrix[] rscB) {
		this.rscB = rscB;
	}

}
