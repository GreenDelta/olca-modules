package org.openlca.io.ilcd.output;

import java.math.BigInteger;

import org.openlca.core.model.Unit;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.ClassificationInfo;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.units.AdminInfo;
import org.openlca.ilcd.units.DataEntry;
import org.openlca.ilcd.units.DataSetInfo;
import org.openlca.ilcd.units.Publication;
import org.openlca.ilcd.units.QuantitativeReference;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.units.UnitGroupInfo;
import org.openlca.ilcd.units.UnitList;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.Reference;
import org.openlca.ilcd.util.UnitExtension;

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
		iUnitGroup.unitGroupInformation = info;
		info.dataSetInformation = makeDataSetInfo();
		info.quantitativeReference = makeQRef();
		iUnitGroup.administrativeInformation = makeAdminInfo();
		iUnitGroup.units = makeUnits();
		config.store.put(iUnitGroup, unitGroup.getRefId());
		this.unitGroup = null;
		return iUnitGroup;
	}

	private DataSetInfo makeDataSetInfo() {
		DataSetInfo dataSetInfo = new DataSetInfo();
		dataSetInfo.uuid = unitGroup.getRefId();
		LangString.addLabel(dataSetInfo.name, unitGroup.getName(),
				config.ilcdConfig);
		if (unitGroup.getDescription() != null)
			LangString.addFreeText(dataSetInfo.generalComment,
					unitGroup.getDescription(), config.ilcdConfig);
		CategoryConverter converter = new CategoryConverter();
		ClassificationInfo classInfo = converter
				.getClassificationInformation(unitGroup.getCategory());
		dataSetInfo.classificationInformation = classInfo;
		return dataSetInfo;
	}

	private QuantitativeReference makeQRef() {
		QuantitativeReference qRef = new QuantitativeReference();
		if (unitGroup.getReferenceUnit() != null)
			qRef.referenceToReferenceUnit = BigInteger.valueOf(0);
		return qRef;
	}

	private UnitList makeUnits() {
		UnitList iUnits = new UnitList();
		Unit refUnit = unitGroup.getReferenceUnit();
		int pos = 1;
		for (Unit unit : unitGroup.getUnits()) {
			org.openlca.ilcd.units.Unit iUnit = makeUnit(unit);
			if (unit.equals(refUnit))
				iUnit.dataSetInternalID = BigInteger.valueOf(0);
			else
				iUnit.dataSetInternalID = BigInteger.valueOf(pos++);
			iUnits.unit.add(iUnit);
		}
		return iUnits;
	}

	private org.openlca.ilcd.units.Unit makeUnit(Unit unit) {
		org.openlca.ilcd.units.Unit iUnit = new org.openlca.ilcd.units.Unit();
		iUnit.meanValue = unit.getConversionFactor();
		iUnit.name = unit.getName();
		if (unit.getDescription() != null) {
			LangString.addLabel(iUnit.generalComment,
					unit.getDescription(), config.ilcdConfig);
		}
		UnitExtension unitExtension = new UnitExtension(iUnit);
		unitExtension.setUnitId(unit.getRefId());
		return iUnit;
	}

	private AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		DataEntry entry = new DataEntry();
		info.dataEntryBy = entry;
		entry.timeStamp = Out.getTimestamp(unitGroup);
		entry.referenceToDataSetFormat.add(
				Reference.forIlcdFormat(config.ilcdConfig));
		addPublication(info);
		return info;
	}

	private void addPublication(AdminInfo info) {
		Publication pub = new Publication();
		info.publicationAndOwnership = pub;
		pub.dataSetVersion = Version.asString(unitGroup.getVersion());
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		pub.permanentDataSetURI = baseUri + "unitgroups/"
				+ unitGroup.getRefId();
	}
}
