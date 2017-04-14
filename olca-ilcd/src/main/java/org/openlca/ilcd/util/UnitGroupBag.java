package org.openlca.ilcd.util;

import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.units.AdminInfo;
import org.openlca.ilcd.units.DataSetInfo;
import org.openlca.ilcd.units.Unit;
import org.openlca.ilcd.units.UnitGroup;

public class UnitGroupBag implements IBag<UnitGroup> {

	private UnitGroup unitGroup;
	private String[] langs;

	public UnitGroupBag(UnitGroup unitGroup, String... langs) {
		this.unitGroup = unitGroup;
		this.langs = langs;
	}

	@Override
	public UnitGroup getValue() {
		return unitGroup;
	}

	public Integer getReferenceUnitId() {
		Unit u = UnitGroups.getReferenceUnit(unitGroup);
		return u == null ? null : u.id;
	}

	public List<Unit> getUnits() {
		return UnitGroups.getUnits(unitGroup);
	}

	@Override
	public String getId() {
		return unitGroup == null ? null : unitGroup.getUUID();
	}

	public String getName() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getFirst(info.name, langs);
		return null;
	}

	public String getComment() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getFirst(info.generalComment, langs);
		return null;
	}

	public List<Category> getSortedClasses() {
		return ClassList.sortedList(unitGroup);
	}

	private DataSetInfo getDataSetInformation() {
		if (unitGroup.unitGroupInfo != null)
			return unitGroup.unitGroupInfo.dataSetInfo;
		return null;
	}

	public String getVersion() {
		if (unitGroup == null)
			return null;
		return unitGroup.getVersion();
	}

	public Date getTimeStamp() {
		if (unitGroup == null)
			return null;
		AdminInfo info = unitGroup.adminInfo;
		if (info == null)
			return null;
		DataEntry entry = info.dataEntry;
		if (entry == null)
			return null;
		XMLGregorianCalendar cal = entry.timeStamp;
		if (cal == null)
			return null;
		else
			return cal.toGregorianCalendar().getTime();
	}

}
