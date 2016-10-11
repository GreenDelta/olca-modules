package org.openlca.ilcd.util;

import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.units.UnitGroup;

public class Reference {

	private Reference() {
	}

	public static DataSetReference forIlcdFormat() {
		DataSetReference ref = new DataSetReference();
		ref.type = DataSetType.SOURCE_DATA_SET;
		ref.uri = "../sources/a97a0155-0234-4b87-b4ce-a45da52f2a40_01.01.000.xml";
		ref.uuid = "a97a0155-0234-4b87-b4ce-a45da52f2a40";
		ref.version = "01.01.000";
		LangString.set(ref.description, "ILCD format", "en");
		return ref;
	}

	public static DataSetReference forUnitGroup(UnitGroup group,
			String... langs) {
		if (group == null)
			return new DataSetReference();
		UnitGroupBag bag = new UnitGroupBag(group, langs);
		DataSetReference ref = new DataSetReference();
		ref.type = DataSetType.UNIT_GROUP_DATA_SET;
		ref.uri = "../unitgroups/" + bag.getId();
		ref.uuid = bag.getId();
		ref.version = bag.getVersion();
		return ref;
	}

}
