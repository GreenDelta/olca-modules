package org.openlca.ilcd.util;

import java.util.GregorianCalendar;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.contacts.AdministrativeInformation;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.ContactInformation;
import org.openlca.ilcd.contacts.DataEntry;
import org.openlca.ilcd.contacts.DataSetInformation;
import org.openlca.ilcd.contacts.Publication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContactBuilder {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private Contact contact;
	private DataSetInformation dataSetInfo;
	private String baseUri;

	private ContactBuilder() {
		contact = new Contact();
		contact.setVersion("1.1");
	}

	public static ContactBuilder makeContact() {
		return new ContactBuilder();
	}

	public ContactBuilder withDataSetInfo(DataSetInformation dataSetInfo) {
		this.dataSetInfo = dataSetInfo;
		return this;
	}

	public ContactBuilder withBaseUri(String baseUri) {
		this.baseUri = baseUri;
		return this;
	}

	public Contact getContact() {
		fill();
		return contact;
	}

	private void fill() {
		fillContactInfo();
		fillAdminInfo();
	}

	private void fillContactInfo() {
		ContactInformation contactInfo = new ContactInformation();
		contact.setContactInformation(contactInfo);
		if (dataSetInfo == null) {
			dataSetInfo = new DataSetInformation();
		}
		contactInfo.setDataSetInformation(dataSetInfo);
		if (dataSetInfo.getUUID() == null) {
			dataSetInfo.setUUID(UUID.randomUUID().toString());
		}
	}

	private void fillAdminInfo() {
		AdministrativeInformation adminInfo = new AdministrativeInformation();
		contact.setAdministrativeInformation(adminInfo);
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
		if(baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if(!baseUri.endsWith("/"))
			baseUri += "/";
		publication.setPermanentDataSetURI(baseUri + "contacts/" + getId());
	}
	
	private String getId() {
		String id = null;
		if(dataSetInfo != null)
			id = dataSetInfo.getUUID();
		return id;
	}
}
