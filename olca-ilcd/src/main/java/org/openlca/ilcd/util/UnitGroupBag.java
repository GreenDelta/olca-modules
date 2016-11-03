package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.AdminInfo;
import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.units.DataSetInfo;
import org.openlca.ilcd.units.QuantitativeReference;
import org.openlca.ilcd.units.Unit;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.units.UnitGroupInfo;
import org.openlca.ilcd.units.UnitList;

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
		UnitGroupInfo info = unitGroup.unitGroupInfo;
		if (info != null) {
			QuantitativeReference qRef = info.quantitativeReference;
			if (qRef != null && qRef.referenceToReferenceUnit != null)
				return qRef.referenceToReferenceUnit.intValue();
		}
		return null;
	}

	public List<Unit> getUnits() {
		UnitList list = unitGroup.units;
		if (list != null)
			return list.unit;
		return Collections.emptyList();
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

	public List<Class> getSortedClasses() {
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
