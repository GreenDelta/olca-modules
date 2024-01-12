package org.openlca.io.ilcd.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.Contacts;

public class ContactImport {

	private final Import imp;
	private Contact contact;
	private Actor actor;

	public ContactImport(Import imp) {
		this.imp = imp;
	}

	public Actor run(Contact contact) {
		this.contact = contact;
		var actor = imp.db().get(Actor.class, contact.getUUID());
		return actor != null
				? actor
				: createNew();
	}

	public static Actor get(Import imp, String id) {
		var actor = imp.db().get(Actor.class, id);
		if (actor != null)
			return actor;
		var dataSet = imp.store().get(Contact.class, id);
		if (dataSet == null) {
			imp.log().error("invalid reference in ILCD data set:" +
					" contact '" + id + "' does not exist");
			return null;
		}
		return new ContactImport(imp).run(dataSet);
	}

	private Actor createNew() {
		actor = new Actor();
		var path = Categories.getPath(contact);
		actor.category = new CategoryDao(imp.db())
				.sync(ModelType.ACTOR, path);
		setDescriptionAttributes();
		setVersionTime();
		return imp.insert(actor);
	}

	private void setDescriptionAttributes() {
		actor.refId = contact.getUUID();

		var info = Contacts.getDataSetInfo(contact);
		actor.name = imp.str(info.name);
		actor.description = imp.str(info.description);
		actor.address = imp.str(info.contactAddress);
		if (actor.address == null) {
			actor.address = imp.str(info.centralContactPoint);
		}
		actor.email = info.email;
		actor.telefax = info.telefax;
		actor.telephone = info.telephone;
		actor.website = info.webSite;
	}

	private void setVersionTime() {
		String v = contact.getVersion();
		actor.version = Version.fromString(v).getValue();
		var entry = Contacts.getDataEntry(contact);
		if (entry.timeStamp != null) {
			actor.lastChange = entry.timeStamp
					.toGregorianCalendar()
					.getTimeInMillis();
		}
	}
}
