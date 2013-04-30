package org.openlca.ilcd.util;

import java.util.GregorianCalendar;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.sources.AdministrativeInformation;
import org.openlca.ilcd.sources.DataEntry;
import org.openlca.ilcd.sources.DataSetInformation;
import org.openlca.ilcd.sources.Publication;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.sources.SourceInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceBuilder {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private Source source;
	private DataSetInformation dataSetInfo;
	private String baseUri;

	private SourceBuilder() {
		source = new Source();
		source.setVersion("1.1");
	}

	public static SourceBuilder makeSource() {
		return new SourceBuilder();
	}

	public SourceBuilder withDataSetInfo(DataSetInformation dataSetInfo) {
		this.dataSetInfo = dataSetInfo;
		return this;
	}

	public SourceBuilder withBaseUri(String baseUri) {
		this.baseUri = baseUri;
		return this;
	}

	public Source getSource() {
		fill();
		return source;
	}

	private void fill() {
		fillSourceInfo();
		fillAdminInfo();
	}

	private void fillSourceInfo() {
		SourceInformation sourceInfo = new SourceInformation();
		source.setSourceInformation(sourceInfo);
		if (dataSetInfo == null) {
			dataSetInfo = new DataSetInformation();
		}
		sourceInfo.setDataSetInformation(dataSetInfo);
		if (dataSetInfo.getUUID() == null) {
			dataSetInfo.setUUID(UUID.randomUUID().toString());
		}
	}

	private void fillAdminInfo() {
		AdministrativeInformation adminInfo = new AdministrativeInformation();
		source.setAdministrativeInformation(adminInfo);
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
		publication.setPermanentDataSetURI(baseUri + "sources/" + getId());
	}

	private String getId() {
		String id = null;
		if (dataSetInfo != null)
			id = dataSetInfo.getUUID();
		return id;
	}
}
