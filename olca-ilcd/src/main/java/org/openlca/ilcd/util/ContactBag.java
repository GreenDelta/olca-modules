package org.openlca.ilcd.util;

import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.contacts.AdminInfo;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.DataSetInfo;

public class ContactBag implements IBag<Contact> {

	private Contact contact;
	private String[] langs;

	public ContactBag(Contact contact, String... langs) {
		this.contact = contact;
		this.langs = langs;
	}

	@Override
	public Contact getValue() {
		return contact;
	}

	@Override
	public String getId() {
		return contact == null ? null : contact.getUUID();
	}

	public String getShortName() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getFirst(info.shortName, langs);
		return null;
	}

	public String getName() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getFirst(info.name, langs);
		return null;
	}

	public List<Category> getSortedClasses() {
		return ClassList.sortedList(contact);
	}

	public String getContactAddress() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getFirst(info.contactAddress, langs);
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
			return LangString.getFirst(info.centralContactPoint, langs);
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
			return LangString.getFirst(info.description, langs);
		return null;
	}

	private DataSetInfo getDataSetInformation() {
		if (contact.contactInfo != null)
			return contact.contactInfo.dataSetInfo;
		return null;
	}

	public String getVersion() {
		if (contact == null)
			return null;
		return contact.getVersion();
	}

	public Date getTimeStamp() {
		if (contact == null)
			return null;
		AdminInfo info = contact.adminInfo;
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
