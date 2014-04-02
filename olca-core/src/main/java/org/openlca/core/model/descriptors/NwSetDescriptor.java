package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class NwSetDescriptor extends BaseDescriptor {

	private static final long serialVersionUID = 2319209556430425517L;

	private String weightedScoreUnit;

	public NwSetDescriptor() {
		setType(ModelType.NW_SET);
	}

	public String getWeightedScoreUnit() {
		return weightedScoreUnit;
	}

	public void setWeightedScoreUnit(String weightedScoreUnit) {
		this.weightedScoreUnit = weightedScoreUnit;
	}

}
