package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;

public class FlowDescriptor extends BaseDescriptor {

	private String locationCode;

	public FlowDescriptor() {
		setType(ModelType.FLOW);
	}

	public String getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	@Override
	public String getDisplayName() {
		String name = getName();
		String disp = name == null ? "no name" : Strings.cut(name, 75);
		if (locationCode != null)
			disp = disp.concat(" (").concat(locationCode).concat(")");
		return disp;
	}

}
