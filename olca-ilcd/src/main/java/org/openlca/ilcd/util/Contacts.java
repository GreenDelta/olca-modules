package org.openlca.ilcd.util;

import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.contacts.AdminInfo;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.ContactInfo;
import org.openlca.ilcd.contacts.DataSetInfo;

public final class Contacts {

	private Contacts() {
	}

	public static ContactInfo getContactInfo(Contact c) {
		if (c == null)
			return null;
		return c.contactInfo;
	}

	public static ContactInfo contactInfo(Contact c) {
		if (c.contactInfo == null)
			c.contactInfo = new ContactInfo();
		return c.contactInfo;
	}

	public static DataSetInfo getDataSetInfo(Contact c) {
		ContactInfo ci = getContactInfo(c);
		if (ci == null)
			return null;
		return ci.dataSetInfo;
	}

	public static DataSetInfo dataSetInfo(Contact c) {
		ContactInfo ci = contactInfo(c);
		if (ci.dataSetInfo == null)
			ci.dataSetInfo = new DataSetInfo();
		return ci.dataSetInfo;
	}

	public static AdminInfo getAdminInfo(Contact c) {
		if (c == null)
			return null;
		return c.adminInfo;
	}

	public static AdminInfo adminInfo(Contact c) {
		if (c.adminInfo == null)
			c.adminInfo = new AdminInfo();
		return c.adminInfo;
	}

	public static DataEntry getDataEntry(Contact c) {
		AdminInfo ai = getAdminInfo(c);
		if (ai == null)
			return null;
		return ai.dataEntry;
	}

	public static DataEntry dataEntry(Contact c) {
		AdminInfo ai = adminInfo(c);
		if (ai.dataEntry == null)
			ai.dataEntry = new DataEntry();
		return ai.dataEntry;
	}

	public static Publication getPublication(Contact c) {
		AdminInfo ai = getAdminInfo(c);
		if (ai == null)
			return null;
		return ai.publication;
	}

	public static Publication publication(Contact c) {
		AdminInfo ai = adminInfo(c);
		if (ai.publication == null)
			ai.publication = new Publication();
		return ai.publication;
	}

}
