package org.openlca.ilcd.units;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UnitsType", propOrder = {
		"units"
})
public class UnitList {

	@XmlElement(name = "unit")
	public final List<Unit> units = new ArrayList<Unit>();

	@Override
	protected UnitList clone() {
		UnitList clone = new UnitList();
		for (Unit u : units) {
			if (u == null)
				continue;
			clone.units.add(u.clone());
		}
		return clone;
	}
}
