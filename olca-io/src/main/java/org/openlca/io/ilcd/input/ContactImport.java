package org.openlca.io.ilcd.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.Contacts;

public class ContactImport {

	private final ImportConfig config;
	private Contact contact;
	private Actor actor;

	public ContactImport(ImportConfig config) {
		this.config = config;
	}

	public Actor run(Contact contact) {
		this.contact = contact;
		var actor = config.db().get(Actor.class, contact.getUUID());
		return actor != null
			? actor
			: createNew();
	}

	public static Actor get(ImportConfig config, String id) {
		var actor = config.db().get(Actor.class, id);
		if (actor != null)
			return actor;
		var dataSet = config.store().get(Contact.class, id);
		if (dataSet == null) {
			config.log().error("invalid reference in ILCD data set:" +
				" contact '" + id + "' does not exist");
			return null;
		}
		return new ContactImport(config).run(dataSet);
	}

	private Actor createNew() {
		actor = new Actor();
		var path = Categories.getPath(contact);
		actor.category = new CategoryDao(config.db())
				.sync(ModelType.ACTOR, path);
		setDescriptionAttributes();
		setVersionTime();
		return config.insert(actor);
	}

	private void setDescriptionAttributes() {
		actor.refId = contact.getUUID();

		var info = Contacts.getDataSetInfo(contact);
		actor.name = config.str(info.name);
		actor.description = config.str(info.description);
		actor.address = config.str(info.contactAddress);
		if (actor.address == null) {
			actor.address = config.str(info.centralContactPoint);
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
