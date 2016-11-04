package org.openlca.ilcd.util;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.units.UnitGroup;

public class Reference {

	private Reference() {
	}

	public static Ref forIlcdFormat() {
		Ref ref = new Ref();
		ref.type = DataSetType.SOURCE;
		ref.uri = "../sources/a97a0155-0234-4b87-b4ce-a45da52f2a40_01.01.000.xml";
		ref.uuid = "a97a0155-0234-4b87-b4ce-a45da52f2a40";
		ref.version = "01.01.000";
		LangString.set(ref.name, "ILCD format", "en");
		return ref;
	}

	public static Ref forUnitGroup(UnitGroup group,
			String... langs) {
		if (group == null)
			return new Ref();
		UnitGroupBag bag = new UnitGroupBag(group, langs);
		Ref ref = new Ref();
		ref.type = DataSetType.UNIT_GROUP;
		ref.uri = "../unitgroups/" + bag.getId();
		ref.uuid = bag.getId();
		ref.version = bag.getVersion();
		return ref;
	}

}
