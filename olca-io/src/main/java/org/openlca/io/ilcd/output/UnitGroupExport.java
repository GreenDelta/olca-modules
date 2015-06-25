package org.openlca.io.ilcd.output;

import java.math.BigInteger;

import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.units.AdministrativeInformation;
import org.openlca.ilcd.units.DataEntry;
import org.openlca.ilcd.units.DataSetInformation;
import org.openlca.ilcd.units.Publication;
import org.openlca.ilcd.units.QuantitativeReference;
import org.openlca.ilcd.units.UnitGroupInformation;
import org.openlca.ilcd.units.UnitList;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.Reference;
import org.openlca.ilcd.util.UnitExtension;

public class UnitGroupExport {

	private UnitGroup unitGroup;
	private DataStore dataStore;
	private String baseUri;

	public UnitGroupExport(DataStore dataStore) {
		this.dataStore = dataStore;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public org.openlca.ilcd.units.UnitGroup run(UnitGroup unitGroup)
			throws DataStoreException {
		this.unitGroup = unitGroup;
		org.openlca.ilcd.units.UnitGroup iUnitGroup = new org.openlca.ilcd.units.UnitGroup();
		iUnitGroup.setVersion("1.1");
		UnitGroupInformation info = new UnitGroupInformation();
		iUnitGroup.setUnitGroupInformation(info);
		info.setDataSetInformation(makeDataSetInfo());
		info.setQuantitativeReference(makeQRef());
		iUnitGroup.setAdministrativeInformation(makeAdminInfo());
		iUnitGroup.setUnits(makeUnits());
		dataStore.put(iUnitGroup, unitGroup.getRefId());
		this.unitGroup = null;
		return iUnitGroup;
	}

	private DataSetInformation makeDataSetInfo() {
		DataSetInformation dataSetInfo = new DataSetInformation();
		dataSetInfo.setUUID(unitGroup.getRefId());
		LangString.addLabel(dataSetInfo.getName(), unitGroup.getName());
		if (unitGroup.getDescription() != null)
			LangString.addFreeText(dataSetInfo.getGeneralComment(),
					unitGroup.getDescription());
		CategoryConverter converter = new CategoryConverter();
		ClassificationInformation classInfo = converter
				.getClassificationInformation(unitGroup.getCategory());
		dataSetInfo.setClassificationInformation(classInfo);
		return dataSetInfo;
	}

	private QuantitativeReference makeQRef() {
		QuantitativeReference qRef = new QuantitativeReference();
		if (unitGroup.getReferenceUnit() != null)
			qRef.setReferenceToReferenceUnit(BigInteger.valueOf(0));
		return qRef;
	}

	private UnitList makeUnits() {
		UnitList iUnits = new UnitList();
		Unit refUnit = unitGroup.getReferenceUnit();
		int pos = 1;
		for (Unit unit : unitGroup.getUnits()) {
			org.openlca.ilcd.units.Unit iUnit = makeUnit(unit);
			if (unit.equals(refUnit))
				iUnit.setDataSetInternalID(BigInteger.valueOf(0));
			else
				iUnit.setDataSetInternalID(BigInteger.valueOf(pos++));
			iUnits.getUnit().add(iUnit);
		}
		return iUnits;
	}

	private org.openlca.ilcd.units.Unit makeUnit(Unit unit) {
		org.openlca.ilcd.units.Unit iUnit = new org.openlca.ilcd.units.Unit();
		iUnit.setMeanValue(unit.getConversionFactor());
		iUnit.setName(unit.getName());
		if (unit.getDescription() != null) {
			LangString.addLabel(iUnit.getGeneralComment(),
					unit.getDescription());
		}
		UnitExtension unitExtension = new UnitExtension(iUnit);
		unitExtension.setUnitId(unit.getRefId());
		return iUnit;
	}

	private AdministrativeInformation makeAdminInfo() {
		AdministrativeInformation info = new AdministrativeInformation();
		DataEntry entry = new DataEntry();
		info.setDataEntryBy(entry);
		entry.setTimeStamp(Out.getTimestamp(unitGroup));
		entry.getReferenceToDataSetFormat().add(Reference.forIlcdFormat());
		addPublication(info);
		return info;
	}

	private void addPublication(AdministrativeInformation info) {
		Publication pub = new Publication();
		info.setPublicationAndOwnership(pub);
		pub.setDataSetVersion(Version.asString(unitGroup.getVersion()));
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		pub.setPermanentDataSetURI(baseUri + "unitgroups/"
				+ unitGroup.getRefId());
	}
}
