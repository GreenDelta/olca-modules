package org.openlca.io.ilcd.output;

import java.util.List;

import org.openlca.core.model.Unit;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.LangString;
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

	private final ExportConfig config;
	private org.openlca.core.model.UnitGroup unitGroup;
	private String baseUri;

	public UnitGroupExport(ExportConfig config) {
		this.config = config;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public UnitGroup run(org.openlca.core.model.UnitGroup unitGroup) {
		if (config.store.contains(UnitGroup.class, unitGroup.refId))
			return config.store.get(UnitGroup.class, unitGroup.refId);
		this.unitGroup = unitGroup;
		UnitGroup iUnitGroup = new UnitGroup();
		iUnitGroup.version = "1.1";
		UnitGroupInfo info = new UnitGroupInfo();
		iUnitGroup.unitGroupInfo = info;
		info.dataSetInfo = makeDataSetInfo();
		info.quantitativeReference = makeQRef();
		iUnitGroup.adminInfo = makeAdminInfo();
		makeUnits(iUnitGroup);
		config.store.put(iUnitGroup);
		this.unitGroup = null;
		return iUnitGroup;
	}

	private DataSetInfo makeDataSetInfo() {
		DataSetInfo dataSetInfo = new DataSetInfo();
		dataSetInfo.uuid = unitGroup.refId;
		LangString.set(dataSetInfo.name, unitGroup.name,
				config.lang);
		if (unitGroup.description != null)
			LangString.set(dataSetInfo.generalComment,
					unitGroup.description, config.lang);
		CategoryConverter converter = new CategoryConverter();
		Classification c = converter.getClassification(
				unitGroup.category);
		if (c != null)
			dataSetInfo.classifications.add(c);
		return dataSetInfo;
	}

	private QuantitativeReference makeQRef() {
		QuantitativeReference qRef = new QuantitativeReference();
		if (unitGroup.referenceUnit != null)
			qRef.referenceUnit = 0;
		return qRef;
	}

	private void makeUnits(UnitGroup iUnitGroup) {
		Unit refUnit = unitGroup.referenceUnit;
		List<org.openlca.ilcd.units.Unit> units = UnitGroups.units(iUnitGroup);
		int pos = 1;
		for (Unit unit : unitGroup.units) {
			org.openlca.ilcd.units.Unit iUnit = makeUnit(unit);
			if (unit.equals(refUnit))
				iUnit.id = 0;
			else
				iUnit.id = pos++;
			units.add(iUnit);
		}
	}

	private org.openlca.ilcd.units.Unit makeUnit(Unit unit) {
		org.openlca.ilcd.units.Unit iUnit = new org.openlca.ilcd.units.Unit();
		iUnit.factor = unit.conversionFactor;
		iUnit.name = unit.name;
		if (unit.description != null) {
			LangString.set(iUnit.comment,
					unit.description, config.lang);
		}
		UnitExtension unitExtension = new UnitExtension(iUnit);
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
