package org.openlca.io.ilcd.output;

import org.openlca.core.model.Unit;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.units.AdminInfo;
import org.openlca.ilcd.units.DataSetInfo;
import org.openlca.ilcd.units.QuantitativeReference;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.units.UnitGroupInfo;
import org.openlca.ilcd.util.Refs;
import org.openlca.ilcd.util.UnitExtension;
import org.openlca.ilcd.util.UnitGroups;
import org.openlca.io.Xml;

public class UnitGroupExport {

	private final ILCDExport exp;
	private org.openlca.core.model.UnitGroup unitGroup;
	private String baseUri;

	public UnitGroupExport(ILCDExport exp) {
		this.exp = exp;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public UnitGroup run(org.openlca.core.model.UnitGroup unitGroup) {
		if (exp.store.contains(UnitGroup.class, unitGroup.refId))
			return exp.store.get(UnitGroup.class, unitGroup.refId);
		this.unitGroup = unitGroup;
		UnitGroup iUnitGroup = new UnitGroup();
		iUnitGroup.version = "1.1";
		UnitGroupInfo info = new UnitGroupInfo();
		iUnitGroup.unitGroupInfo = info;
		info.dataSetInfo = makeDataSetInfo();
		info.quantitativeReference = makeQRef();
		iUnitGroup.adminInfo = makeAdminInfo();
		makeUnits(iUnitGroup);
		exp.store.put(iUnitGroup);
		this.unitGroup = null;
		return iUnitGroup;
	}

	private DataSetInfo makeDataSetInfo() {
		var info = new DataSetInfo();
		info.uuid = unitGroup.refId;
		exp.add(info.name, unitGroup.name);
		exp.add(info.generalComment, unitGroup.description);
		var converter = new CategoryConverter();
		var c = converter.getClassification(unitGroup.category);
		if (c != null) {
			info.classifications.add(c);
		}
		return info;
	}

	private QuantitativeReference makeQRef() {
		var qRef = new QuantitativeReference();
		if (unitGroup.referenceUnit != null) {
			qRef.referenceUnit = 0;
		}
		return qRef;
	}

	private void makeUnits(UnitGroup iUnitGroup) {
		Unit refUnit = unitGroup.referenceUnit;
		var units = UnitGroups.units(iUnitGroup);
		int pos = 1;
		for (Unit unit : unitGroup.units) {
			org.openlca.ilcd.units.Unit iUnit = makeUnit(unit);
			iUnit.id = unit.equals(refUnit)
					? 0
					: pos++;
			units.add(iUnit);
		}
	}

	private org.openlca.ilcd.units.Unit makeUnit(Unit unit) {
		var iUnit = new org.openlca.ilcd.units.Unit();
		iUnit.factor = unit.conversionFactor;
		iUnit.name = unit.name;
		exp.add(iUnit.comment, unit.description);
		var unitExtension = new UnitExtension(iUnit);
		unitExtension.setUnitId(unit.refId);
		return iUnit;
	}

	private AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		DataEntry entry = new DataEntry();
		info.dataEntry = entry;
		entry.timeStamp = Xml.calendar(unitGroup.lastChange);
		entry.formats.add(Refs.ilcd());
		addPublication(info);
		return info;
	}

	private void addPublication(AdminInfo info) {
		Publication pub = new Publication();
		info.publication = pub;
		pub.version = Version.asString(unitGroup.version);
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		pub.uri = baseUri + "unitgroups/" + unitGroup.refId;
	}
}
