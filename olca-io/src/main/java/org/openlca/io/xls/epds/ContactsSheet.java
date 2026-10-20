package org.openlca.io.xls.epds;

import org.openlca.commons.Strings;
import org.openlca.core.model.Actor;

/// Writes the "Contacts" sheet with one section per referenced actor.
class ContactsSheet {

	private final WriterContext ctx;

	ContactsSheet(WriterContext ctx) {
		this.ctx = ctx;
	}

	void write() {
		var sheet = ctx.writer(Tab.CONTACTS);
		var contacts = ctx.contacts();
		contacts.sort((a1, a2) -> Strings.compareNatural(a1.name, a2.name));
		for (var contact : contacts) {
			writeContact(sheet, contact);
		}
	}

	private void writeContact(SheetWriter sheet, Actor contact) {
		sheet.section(contact.name)
			.metaDataFieldsOf(contact)
			.field(Field.ADDRESS, contact.address)
			.field(Field.CITY, contact.city)
			.field(Field.ZIP_CODE, contact.zipCode)
			.field(Field.COUNTRY, contact.country)
			.field(Field.EMAIL, contact.email)
			.field(Field.TELEFAX, contact.telefax)
			.field(Field.TELEPHONE, contact.telephone)
			.field(Field.WEBSITE, contact.website)
			.blank();
	}

}
