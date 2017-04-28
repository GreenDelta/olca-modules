package org.openlca.io;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class UnitMappingEntry {

	public String unitName;
	public Unit unit;
	public UnitGroup unitGroup;
	public FlowProperty flowProperty;
	public Double factor;

	public boolean isValid() {
		return unit != null
				&& unitGroup != null
				&& flowProperty != null
				&& factor != null;
	}

}
