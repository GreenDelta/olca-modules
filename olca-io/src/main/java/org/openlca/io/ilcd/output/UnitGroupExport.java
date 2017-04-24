package org.openlca.io.ilcd.output;

import java.util.List;

import org.openlca.core.model.Unit;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.units.AdminInfo;
import org.openlca.ilcd.units.DataSetInfo;
import org.openlca.ilcd.units.QuantitativeReference;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.units.UnitGroupInfo;
import org.openlca.ilcd.util.Refs;
import org.openlca.ilcd.util.UnitExtension;
import org.openlca.ilcd.util.UnitGroups;

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

	public UnitGroup run(org.openlca.core.model.UnitGroup unitGroup)
			throws DataStoreException {
		if (config.store.contains(UnitGroup.class, unitGroup.getRefId()))
			return config.store.get(UnitGroup.class, unitGroup.getRefId());
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
		dataSetInfo.uuid = unitGroup.getRefId();
		LangString.set(dataSetInfo.name, unitGroup.getName(),
				config.lang);
		if (unitGroup.getDescription() != null)
			LangString.set(dataSetInfo.generalComment,
					unitGroup.getDescription(), config.lang);
		CategoryConverter converter = new CategoryConverter();
		Classification c = converter.getClassification(
				unitGroup.getCategory());
		if (c != null)
			dataSetInfo.classifications.add(c);
		return dataSetInfo;
	}

	private QuantitativeReference makeQRef() {
		QuantitativeReference qRef = new QuantitativeReference();
		if (unitGroup.getReferenceUnit() != null)
			qRef.referenceUnit = 0;
		return qRef;
	}

	private void makeUnits(UnitGroup iUnitGroup) {
		Unit refUnit = unitGroup.getReferenceUnit();
		List<org.openlca.ilcd.units.Unit> units = UnitGroups.units(iUnitGroup);
		int pos = 1;
		for (Unit unit : unitGroup.getUnits()) {
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
		iUnit.factor = unit.getConversionFactor();
		iUnit.name = unit.getName();
		if (unit.getDescription() != null) {
			LangString.set(iUnit.comment,
					unit.getDescription(), config.lang);
		}
		UnitExtension unitExtension = new UnitExtension(iUnit);
		unitExtension.setUnitId(unit.getRefId());
		return iUnit;
	}

	private AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		DataEntry entry = new DataEntry();
		info.dataEntry = entry;
		entry.timeStamp = Out.getTimestamp(unitGroup);
		entry.formats.add(Refs.ilcd());
		addPublication(info);
		return info;
	}

	private void addPublication(AdminInfo info) {
		Publication pub = new Publication();
		info.publication = pub;
		pub.version = Version.asString(unitGroup.getVersion());
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		pub.uri = baseUri + "unitgroups/" + unitGroup.getRefId();
	}
}
