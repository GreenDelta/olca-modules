package org.openlca.io.ilcd.input;

import java.util.Date;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.ContactBag;

public class ContactImport {

	private final ImportConfig config;
	private ContactBag ilcdContact;
	private Actor actor;

	public ContactImport(ImportConfig config) {
		this.config = config;
	}

	public Actor run(Contact dataSet) {
		this.ilcdContact = new ContactBag(dataSet, config.langOrder());
		var actor = config.db().get(Actor.class, ilcdContact.getId());
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
		var path = Categories.getPath(ilcdContact.getValue());
		actor.category = new CategoryDao(config.db())
				.sync(ModelType.ACTOR, path);
		setDescriptionAttributes();
		setVersionTime();
		return config.insert(actor);
	}

	private void setDescriptionAttributes() {
		actor.refId = ilcdContact.getId();
		actor.name = ilcdContact.getName();
		actor.description = ilcdContact.getComment();
		actor.address = ilcdContact.getContactAddress();
		if (actor.address == null) {
			actor.address = ilcdContact.getCentralContactPoint();
		}
		actor.email = ilcdContact.getEmail();
		actor.telefax = ilcdContact.getTelefax();
		actor.telephone = ilcdContact.getTelephone();
		actor.website = ilcdContact.getWebSite();
	}

	private void setVersionTime() {
		String v = ilcdContact.getVersion();
		actor.version = Version.fromString(v).getValue();
		Date time = ilcdContact.getTimeStamp();
		if (time != null)
			actor.lastChange = time.getTime();
	}
}
