package org.openlca.ilcd.util;

import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.units.AdministrativeInformation;
import org.openlca.ilcd.units.DataEntry;
import org.openlca.ilcd.units.DataSetInformation;
import org.openlca.ilcd.units.Publication;
import org.openlca.ilcd.units.QuantitativeReference;
import org.openlca.ilcd.units.Unit;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.units.UnitGroupInformation;
import org.openlca.ilcd.units.UnitList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitGroupBuilder {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private UnitGroup unitGroup;
	private DataSetInformation dataSetInfo;
	private String baseUri;
	private Integer refUnitId;
	private List<Unit> units;

	private UnitGroupBuilder() {
		unitGroup = new UnitGroup();
		unitGroup.setVersion("1.1");
	}

	public static UnitGroupBuilder makeUnitGroup() {
		return new UnitGroupBuilder();
	}

	public UnitGroupBuilder withDataSetInfo(DataSetInformation dataSetInfo) {
		this.dataSetInfo = dataSetInfo;
		return this;
	}

	public UnitGroupBuilder withBaseUri(String baseUri) {
		this.baseUri = baseUri;
		return this;
	}

	public UnitGroupBuilder withReferenceUnitId(Integer id) {
		this.refUnitId = id;
		return this;
	}

	public UnitGroupBuilder withUnits(List<Unit> units) {
		this.units = units;
		return this;
	}

	public UnitGroup getUnitGroup() {
		fill();
		return unitGroup;
	}

	private void fill() {
		fillUnitGroupInfo();
		fillAdminInfo();
		fillUnits();
	}

	private void fillUnitGroupInfo() {
		UnitGroupInformation unitGroupInfo = new UnitGroupInformation();
		unitGroup.setUnitGroupInformation(unitGroupInfo);
		if (dataSetInfo == null) {
			dataSetInfo = new DataSetInformation();
		}
		unitGroupInfo.setDataSetInformation(dataSetInfo);
		if (dataSetInfo.getUUID() == null) {
			dataSetInfo.setUUID(UUID.randomUUID().toString());
		}
		QuantitativeReference qRef = new QuantitativeReference();
		unitGroupInfo.setQuantitativeReference(qRef);
		if (refUnitId != null)
			qRef.setReferenceToReferenceUnit(BigInteger.valueOf(refUnitId));
	}

	private void fillAdminInfo() {
		AdministrativeInformation adminInfo = new AdministrativeInformation();
		unitGroup.setAdministrativeInformation(adminInfo);
		DataEntry dataEntry = new DataEntry();
		adminInfo.setDataEntryBy(dataEntry);
		setTimeStamp(dataEntry);
		dataEntry.getReferenceToDataSetFormat().add(Reference.forIlcdFormat());
		fillPublication(adminInfo);
	}

	private void setTimeStamp(DataEntry dataEntry) {
		try {
			XMLGregorianCalendar calendar = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(new GregorianCalendar());
			dataEntry.setTimeStamp(calendar);
		} catch (Exception e) {
			log.error("Cannot set timestamp", e);
		}
	}

	private void fillPublication(AdministrativeInformation adminInfo) {
		Publication publication = new Publication();
		adminInfo.setPublicationAndOwnership(publication);
		publication.setDataSetVersion("01.00.000");
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		publication.setPermanentDataSetURI(baseUri + "unitgroups/" + getId());
	}

	private String getId() {
		String id = null;
		if (dataSetInfo != null)
			id = dataSetInfo.getUUID();
		return id;
	}

	private void fillUnits() {
		UnitList unitList = new UnitList();
		unitGroup.setUnits(unitList);
		if (units != null)
			unitList.getUnit().addAll(units);
	}
}
