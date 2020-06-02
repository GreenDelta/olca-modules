package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class LocationDescriptor extends CategorizedDescriptor {

	public String code;

	public LocationDescriptor() {
		this.type = ModelType.LOCATION;
	}

}
