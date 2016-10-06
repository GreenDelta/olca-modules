package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.ClassificationInfo;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.contacts.AdminInfo;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.DataEntry;
import org.openlca.ilcd.contacts.DataSetInfo;
import org.openlca.ilcd.contacts.Publication;

public class ContactBag implements IBag<Contact> {

	private Contact contact;
	private IlcdConfig config;

	public ContactBag(Contact contact, IlcdConfig config) {
		this.contact = contact;
		this.config = config;
	}

	@Override
	public Contact getValue() {
		return contact;
	}

	@Override
	public String getId() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return info.uuid;
		return null;
	}

	public String getShortName() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getVal(info.shortName, config);
		return null;
	}

	public String getName() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getVal(info.name, config);
		return null;
	}

	public List<Class> getSortedClasses() {
		DataSetInfo info = getDataSetInformation();
		if (info != null) {
			ClassificationInfo classInfo = info.classificationInformation;
			return ClassList.sortedList(classInfo);
		}
		return Collections.emptyList();
	}

	public String getContactAddress() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getVal(info.contactAddress, config);
		return null;
	}

	public String getTelephone() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return info.telephone;
		return null;
	}

	public String getTelefax() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return info.telefax;
		return null;
	}

	public String getWebSite() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return info.wwwAddress;
		return null;
	}

	public String getCentralContactPoint() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getVal(info.centralContactPoint, config);
		return null;
	}

	public String getEmail() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return info.email;
		return null;
	}

	public String getComment() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getVal(info.description, config);
		return null;
	}

	private DataSetInfo getDataSetInformation() {
		if (contact.contactInformation != null)
			return contact.contactInformation.getDataSetInformation();
		return null;
	}

	public String getVersion() {
		if (contact == null)
			return null;
		AdminInfo info = contact.administrativeInformation;
		if (info == null)
			return null;
		Publication pub = info.publication;
		if (pub == null)
			return null;
		else
			return pub.dataSetVersion;
	}

	public Date getTimeStamp() {
		if (contact == null)
			return null;
		AdminInfo info = contact.administrativeInformation;
		if (info == null)
			return null;
		DataEntry entry = info.dataEntry;
		if (entry == null)
			return null;
		XMLGregorianCalendar cal = entry.timeStamp;
		if (cal == null)
			return null;
		else
			return cal.toGregorianCalendar().getTime();
	}

}
