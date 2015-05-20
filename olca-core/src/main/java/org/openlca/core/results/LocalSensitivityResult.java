package org.openlca.core.results;

import org.openlca.core.math.IMatrix;

public class LocalSensitivityResult extends ContributionResult {
	private IMatrix[] techCoefficients;
	private IMatrix[] enviCoefficients;
	
	public IMatrix getTechCoefficients(int impact) {
		return techCoefficients[impact];
	}

	public void setTechCoefficients(IMatrix[] techCoefficients) {
		this.techCoefficients = techCoefficients;
	}

	public IMatrix getEnviCoefficients(int impact) {
		return enviCoefficients[impact];
	}

	public void setEnviCoefficients(IMatrix[] enviCoefficients) {
		this.enviCoefficients = enviCoefficients;
	}

}
