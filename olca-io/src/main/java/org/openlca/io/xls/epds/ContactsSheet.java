package org.openlca.io.xls.epds;

import java.util.Date;

import org.openlca.commons.Strings;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Version;
import org.openlca.util.Categories;

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
			.field(Field.UUID, contact.refId)
			.field(Field.CATEGORY, categoryOf(contact))
			.field(Field.VERSION, Version.asString(contact.version))
			.dateTime(Field.LAST_CHANGE, contact.lastChange > 0
				? new Date(contact.lastChange)
				: null)
			.field(Field.TAGS, contact.tags)
			.field(Field.DESCRIPTION, contact.description)
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

	private static String categoryOf(Actor contact) {
		if (contact.category == null)
			return null;
		var path = Categories.path(contact.category);
		return path.isEmpty()
			? null
			: String.join("/", path);
	}
}
