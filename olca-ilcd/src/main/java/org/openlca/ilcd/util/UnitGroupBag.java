package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.units.AdministrativeInformation;
import org.openlca.ilcd.units.DataEntry;
import org.openlca.ilcd.units.DataSetInformation;
import org.openlca.ilcd.units.Publication;
import org.openlca.ilcd.units.QuantitativeReference;
import org.openlca.ilcd.units.Unit;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.units.UnitGroupInformation;
import org.openlca.ilcd.units.UnitList;

public class UnitGroupBag implements IBag<UnitGroup> {

	private UnitGroup unitGroup;

	public UnitGroupBag(UnitGroup unitGroup) {
		this.unitGroup = unitGroup;
	}

	@Override
	public UnitGroup getValue() {
		return unitGroup;
	}

	public Integer getReferenceUnitId() {
		UnitGroupInformation info = unitGroup.getUnitGroupInformation();
		if (info != null) {
			QuantitativeReference qRef = info.getQuantitativeReference();
			if (qRef != null && qRef.getReferenceToReferenceUnit() != null)
				return qRef.getReferenceToReferenceUnit().intValue();
		}
		return null;
	}

	public List<Unit> getUnits() {
		UnitList list = unitGroup.getUnits();
		if (list != null)
			return list.getUnit();
		return Collections.emptyList();
	}

	@Override
	public String getId() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return info.getUUID();
		return null;
	}

	public String getName() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.getName());
		return null;
	}

	public String getComment() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.getGeneralComment());
		return null;
	}

	public List<Class> getSortedClasses() {
		DataSetInformation info = getDataSetInformation();
		if (info != null) {
			ClassificationInformation classInfo = info
					.getClassificationInformation();
			return ClassList.sortedList(classInfo);
		}
		return Collections.emptyList();
	}

	private DataSetInformation getDataSetInformation() {
		if (unitGroup.getUnitGroupInformation() != null)
			return unitGroup.getUnitGroupInformation().getDataSetInformation();
		return null;
	}

	public String getVersion() {
		if (unitGroup == null)
			return null;
		AdministrativeInformation info = unitGroup
				.getAdministrativeInformation();
		if (info == null)
			return null;
		Publication pub = info.getPublicationAndOwnership();
		if (pub == null)
			return null;
		else
			return pub.getDataSetVersion();
	}

	public Date getTimeStamp() {
		if (unitGroup == null)
			return null;
		AdministrativeInformation info = unitGroup
				.getAdministrativeInformation();
		if (info == null)
			return null;
		DataEntry entry = info.getDataEntryBy();
		if (entry == null)
			return null;
		XMLGregorianCalendar cal = entry.getTimeStamp();
		if (cal == null)
			return null;
		else
			return cal.toGregorianCalendar().getTime();
	}

}
