package org.openlca.ilcd.util;

import java.util.GregorianCalendar;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.flowproperties.AdministrativeInformation;
import org.openlca.ilcd.flowproperties.DataEntry;
import org.openlca.ilcd.flowproperties.DataSetInformation;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.FlowPropertyInformation;
import org.openlca.ilcd.flowproperties.Publication;
import org.openlca.ilcd.flowproperties.QuantitativeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowPropertyBuilder {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private FlowProperty flowProperty;
	private DataSetInformation dataSetInfo;
	private String baseUri;
	private DataSetReference unitGroupRef;

	private FlowPropertyBuilder() {
		flowProperty = new FlowProperty();
		flowProperty.setVersion("1.1");
	}

	public static FlowPropertyBuilder makeFlowProperty() {
		return new FlowPropertyBuilder();
	}

	public FlowPropertyBuilder withBaseUri(String baseUri) {
		this.baseUri = baseUri;
		return this;
	}

	public FlowPropertyBuilder withUnitGroupReference(
			DataSetReference unitGroupRef) {
		this.unitGroupRef = unitGroupRef;
		return this;
	}

	public FlowPropertyBuilder withDataSetInfo(DataSetInformation dataSetInfo) {
		this.dataSetInfo = dataSetInfo;
		return this;
	}

	public FlowProperty getFlowProperty() {
		fill();
		return flowProperty;
	}

	private void fill() {
		fillPropertyInfo();
		fillAdminInfo();
	}

	private void fillPropertyInfo() {
		FlowPropertyInformation info = new FlowPropertyInformation();
		flowProperty.setFlowPropertyInformation(info);
		if (dataSetInfo == null) {
			dataSetInfo = new DataSetInformation();
		}
		info.setDataSetInformation(dataSetInfo);
		if (dataSetInfo.getUUID() == null) {
			dataSetInfo.setUUID(UUID.randomUUID().toString());
		}
		QuantitativeReference qRef = new QuantitativeReference();
		info.setQuantitativeReference(qRef);
		if (unitGroupRef != null)
			qRef.setUnitGroup(unitGroupRef);
	}

	private void fillAdminInfo() {
		AdministrativeInformation adminInfo = new AdministrativeInformation();
		flowProperty.setAdministrativeInformation(adminInfo);
		DataEntry dataEntry = new DataEntry();
		adminInfo.setDataEntry(dataEntry);
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
		adminInfo.setPublication(publication);
		publication.setDataSetVersion("01.00.000");
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		publication.setPermanentDataSetURI(baseUri + "flowproperties/"
				+ getId());
	}

	private String getId() {
		String id = null;
		if (dataSetInfo != null)
			id = dataSetInfo.getUUID();
		return id;
	}

}
