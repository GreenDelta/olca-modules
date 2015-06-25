package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.contacts.AdministrativeInformation;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.DataEntry;
import org.openlca.ilcd.contacts.DataSetInformation;
import org.openlca.ilcd.contacts.Publication;

public class ContactBag implements IBag<Contact> {

	private Contact contact;

	public ContactBag(Contact contact) {
		this.contact = contact;
	}

	@Override
	public Contact getValue() {
		return contact;
	}

	@Override
	public String getId() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return info.getUUID();
		return null;
	}

	public String getShortName() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.getShortName());
		return null;
	}

	public String getName() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.getName());
		return null;
	}

	public List<Class> getSortedClasses() {
		DataSetInformation info = getDataSetInformation();
		if (info != null) {
			ClassificationInformation classInfo = info
					.getClassificationInformation();
			return ClassList.sortedList(classInfo);
		}
		return Collections.emptyList();
	}

	public String getContactAddress() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.getContactAddress());
		return null;
	}

	public String getTelephone() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return info.getTelephone();
		return null;
	}

	public String getTelefax() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return info.getTelefax();
		return null;
	}

	public String getWebSite() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return info.getWWWAddress();
		return null;
	}

	public String getCentralContactPoint() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.getCentralContactPoint());
		return null;
	}

	public String getEmail() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return info.getEmail();
		return null;
	}

	public String getComment() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.getDescription());
		return null;
	}

	private DataSetInformation getDataSetInformation() {
		if (contact.getContactInformation() != null)
			return contact.getContactInformation().getDataSetInformation();
		return null;
	}

	public String getVersion() {
		if (contact == null)
			return null;
		AdministrativeInformation info = contact.getAdministrativeInformation();
		if (info == null)
			return null;
		Publication pub = info.getPublication();
		if (pub == null)
			return null;
		else
			return pub.getDataSetVersion();
	}

	public Date getTimeStamp() {
		if (contact == null)
			return null;
		AdministrativeInformation info = contact.getAdministrativeInformation();
		if (info == null)
			return null;
		DataEntry entry = info.getDataEntry();
		if (entry == null)
			return null;
		XMLGregorianCalendar cal = entry.getTimeStamp();
		if (cal == null)
			return null;
		else
			return cal.toGregorianCalendar().getTime();
	}

}
