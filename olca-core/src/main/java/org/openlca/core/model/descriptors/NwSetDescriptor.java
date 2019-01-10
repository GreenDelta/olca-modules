package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class NwSetDescriptor extends BaseDescriptor {

	public String weightedScoreUnit;

	public NwSetDescriptor() {
		this.type = ModelType.NW_SET;
	}

}
