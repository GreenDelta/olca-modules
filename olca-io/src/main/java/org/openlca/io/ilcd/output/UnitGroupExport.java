package org.openlca.io.ilcd.output;

import org.openlca.core.model.Unit;
import org.openlca.core.model.Version;
import org.openlca.ilcd.units.AdminInfo;
import org.openlca.ilcd.units.DataSetInfo;
import org.openlca.ilcd.units.QuantitativeReference;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.Refs;
import org.openlca.ilcd.util.UnitExtension;
import org.openlca.io.Xml;

public class UnitGroupExport {

	private final Export exp;
	private final org.openlca.core.model.UnitGroup unitGroup;
	private String baseUri;

	public UnitGroupExport(Export exp, org.openlca.core.model.UnitGroup unitGroup) {
		this.exp = exp;
		this.unitGroup = unitGroup;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public void write() {
		if (exp.store.contains(UnitGroup.class, unitGroup.refId))
			return;
		var ds = new UnitGroup()
				.withAdminInfo(makeAdminInfo());
		ds.withUnitGroupInfo()
				.withDataSetInfo(makeDataSetInfo())
				.withQuantitativeReference(makeQRef());
		makeUnits(ds);
		exp.store.put(ds);
	}

	private DataSetInfo makeDataSetInfo() {
		var info = new DataSetInfo()
				.withUUID(unitGroup.refId);
		exp.add(info::withName, unitGroup.name);
		exp.add(info::withComment, unitGroup.description);
		Categories.toClassification(unitGroup.category, info::withClassifications);
		return info;
	}

	private QuantitativeReference makeQRef() {
		var qRef = new QuantitativeReference();
		if (unitGroup.referenceUnit != null) {
			qRef.withReferenceUnit(0);
		}
		return qRef;
	}

	private void makeUnits(UnitGroup ds) {
		Unit refUnit = unitGroup.referenceUnit;
		var units = ds.withUnits();
		int pos = 1;
		for (Unit unit : unitGroup.units) {
			var iUnit = makeUnit(unit);
			iUnit.withId(unit.equals(refUnit) ? 0 : pos++);
			units.add(iUnit);
		}
	}

	private org.openlca.ilcd.units.Unit makeUnit(Unit unit) {
		var iUnit = new org.openlca.ilcd.units.Unit()
				.withFactor(unit.conversionFactor)
				.withName(unit.name);
		exp.add(iUnit::withComment, unit.description);
		var unitExtension = new UnitExtension(iUnit);
		unitExtension.setUnitId(unit.refId);
		return iUnit;
	}

	private AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		info.withDataEntry()
				.withTimeStamp(Xml.calendar(unitGroup.lastChange))
				.withFormats()
				.add(Refs.ilcd());
		addPublication(info);
		return info;
	}

	private void addPublication(AdminInfo info) {
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		info.withPublication()
				.withVersion(Version.asString(unitGroup.version))
				.withUri(baseUri + "unitgroups/" + unitGroup.refId);
	}
}
