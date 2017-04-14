package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.units.AdminInfo;
import org.openlca.ilcd.units.DataSetInfo;
import org.openlca.ilcd.units.QuantitativeReference;
import org.openlca.ilcd.units.Unit;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.units.UnitGroupInfo;
import org.openlca.ilcd.units.UnitList;

public final class UnitGroups {

	private UnitGroups() {
	}

	public static UnitGroupInfo getUnitGroupInfo(UnitGroup u) {
		if (u == null)
			return null;
		return u.unitGroupInfo;
	}

	public static UnitGroupInfo unitGroupInfo(UnitGroup u) {
		if (u.unitGroupInfo == null)
			u.unitGroupInfo = new UnitGroupInfo();
		return u.unitGroupInfo;
	}

	public static DataSetInfo getDataSetInfo(UnitGroup u) {
		UnitGroupInfo ugi = getUnitGroupInfo(u);
		if (ugi == null)
			return null;
		return ugi.dataSetInfo;
	}

	public static DataSetInfo dataSetInfo(UnitGroup u) {
		UnitGroupInfo ugi = unitGroupInfo(u);
		if (ugi.dataSetInfo == null)
			ugi.dataSetInfo = new DataSetInfo();
		return ugi.dataSetInfo;
	}

	public static QuantitativeReference getQuantitativeReference(UnitGroup u) {
		UnitGroupInfo ugi = getUnitGroupInfo(u);
		if (ugi == null)
			return null;
		return ugi.quantitativeReference;
	}

	public static QuantitativeReference quantitativeReference(UnitGroup u) {
		UnitGroupInfo ugi = unitGroupInfo(u);
		if (ugi.quantitativeReference == null)
			ugi.quantitativeReference = new QuantitativeReference();
		return ugi.quantitativeReference;
	}

	public static AdminInfo getAdminInfo(UnitGroup u) {
		if (u == null)
			return null;
		return u.adminInfo;
	}

	public static AdminInfo adminInfo(UnitGroup u) {
		if (u.adminInfo == null)
			u.adminInfo = new AdminInfo();
		return u.adminInfo;
	}

	public static DataEntry getDataEntry(UnitGroup u) {
		AdminInfo ai = getAdminInfo(u);
		if (ai == null)
			return null;
		return ai.dataEntry;
	}

	public static DataEntry dataEntry(UnitGroup u) {
		AdminInfo ai = adminInfo(u);
		if (ai.dataEntry == null)
			ai.dataEntry = new DataEntry();
		return ai.dataEntry;
	}

	public static Publication getPublication(UnitGroup u) {
		AdminInfo ai = getAdminInfo(u);
		if (ai == null)
			return null;
		return ai.publication;
	}

	public static Publication publication(UnitGroup u) {
		AdminInfo ai = adminInfo(u);
		if (ai.publication == null)
			ai.publication = new Publication();
		return ai.publication;
	}

	public static List<Unit> getUnits(UnitGroup u) {
		if (u == null || u.unitList == null)
			return Collections.emptyList();
		return u.unitList.units;
	}

	public static List<Unit> units(UnitGroup u) {
		if (u.unitList == null)
			u.unitList = new UnitList();
		return u.unitList.units;
	}

	public static Unit getReferenceUnit(UnitGroup u) {
		QuantitativeReference qRef = getQuantitativeReference(u);
		if (qRef == null)
			return null;
		for (Unit unit : getUnits(u)) {
			if (unit == null)
				continue;
			if (unit.id == qRef.referenceUnit)
				return unit;
		}
		return null;
	}
}
