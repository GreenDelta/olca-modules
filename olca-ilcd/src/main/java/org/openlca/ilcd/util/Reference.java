package org.openlca.ilcd.util;

import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.units.UnitGroup;

public class Reference {

	private Reference() {		
	}
	
	public static DataSetReference forIlcdFormat() {
		DataSetReference ref = new DataSetReference();
		ref.setType(DataSetType.SOURCE_DATA_SET);
		ref.setUri("../sources/a97a0155-0234-4b87-b4ce-a45da52f2a40_01.01.000.xml");
		ref.setUuid("a97a0155-0234-4b87-b4ce-a45da52f2a40");
		ref.setVersion("01.01.000");
		LangString.addShortText(ref.getShortDescription(), "ILCD format");
		return ref;
	}
	
	public static DataSetReference forUnitGroup(UnitGroup group) {
		if(group == null)
			return new DataSetReference();
		UnitGroupBag bag = new UnitGroupBag(group);
		DataSetReference ref = new DataSetReference();
		ref.setType(DataSetType.UNIT_GROUP_DATA_SET);
		ref.setUri("../unitgroups/" + bag.getId());
		ref.setUuid(bag.getId());
		ref.setVersion(bag.getVersion());
		return ref;
	}
	
}
