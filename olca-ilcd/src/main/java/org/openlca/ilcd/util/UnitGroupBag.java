package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.units.AdministrativeInformation;
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
			if (qRef != null && qRef.getReferenceToReferenceUnit() != null) {
				return qRef.getReferenceToReferenceUnit().intValue();
			}
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
			return LangString.getLabel(info.getName());
		return null;
	}

	public String getComment() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return LangString.getFreeText(info.getGeneralComment());
		return null;
	}

	public String getVersion() {
		String version = null;
		AdministrativeInformation adminInfo = unitGroup
				.getAdministrativeInformation();
		if (adminInfo != null) {
			Publication pub = adminInfo.getPublicationAndOwnership();
			if (pub != null) {
				version = pub.getDataSetVersion();
			}
		}
		return version;
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

}
