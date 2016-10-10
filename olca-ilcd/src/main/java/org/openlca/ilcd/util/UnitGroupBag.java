package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.ClassificationInfo;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.units.AdminInfo;
import org.openlca.ilcd.units.DataEntry;
import org.openlca.ilcd.units.DataSetInfo;
import org.openlca.ilcd.units.Publication;
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
		UnitGroupInfo info = unitGroup.unitGroupInformation;
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
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return info.uuid;
		return null;
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
		DataSetInfo info = getDataSetInformation();
		if (info != null) {
			ClassificationInfo classInfo = info.classificationInformation;
			return ClassList.sortedList(classInfo);
		}
		return Collections.emptyList();
	}

	private DataSetInfo getDataSetInformation() {
		if (unitGroup.unitGroupInformation != null)
			return unitGroup.unitGroupInformation.dataSetInformation;
		return null;
	}

	public String getVersion() {
		if (unitGroup == null)
			return null;
		AdminInfo info = unitGroup.administrativeInformation;
		if (info == null)
			return null;
		Publication pub = info.publicationAndOwnership;
		if (pub == null)
			return null;
		else
			return pub.dataSetVersion;
	}

	public Date getTimeStamp() {
		if (unitGroup == null)
			return null;
		AdminInfo info = unitGroup.administrativeInformation;
		if (info == null)
			return null;
		DataEntry entry = info.dataEntryBy;
		if (entry == null)
			return null;
		XMLGregorianCalendar cal = entry.timeStamp;
		if (cal == null)
			return null;
		else
			return cal.toGregorianCalendar().getTime();
	}

}
