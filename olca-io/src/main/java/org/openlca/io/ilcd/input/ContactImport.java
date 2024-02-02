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
	private final Contact ds;
	private Actor actor;

	public ContactImport(Import imp, Contact ds) {
		this.imp = imp;
		this.ds = ds;
	}

	public Actor run() {
		var id = Contacts.getUUID(ds);
		if (id == null) {
			imp.log().error("failed to read UUID from contact: " + ds);
			return null;
		}
		var actor = imp.db().get(Actor.class, id);
		return actor != null
				? actor
				: createNew();
	}

	public static Actor get(Import imp, String id) {
		var actor = imp.db().get(Actor.class, id);
		if (actor != null)
			return actor;
		var ds = imp.store().get(Contact.class, id);
		if (ds == null) {
			imp.log().error("invalid reference in ILCD data set:" +
					" contact '" + id + "' does not exist");
			return null;
		}
		return new ContactImport(imp, ds).run();
	}

	private Actor createNew() {
		actor = new Actor();
		var path = Categories.getPath(ds);
		actor.category = new CategoryDao(imp.db())
				.sync(ModelType.ACTOR, path);
		setDescriptionAttributes();
		setVersionTime();
		return imp.insert(actor);
	}

	private void setDescriptionAttributes() {
		actor.refId = Contacts.getUUID(ds);

		var info = Contacts.getDataSetInfo(ds);
		actor.name = imp.str(info.getName());
		actor.description = imp.str(info.getDescription());
		actor.address = imp.str(info.getContactAddress());
		if (actor.address == null) {
			actor.address = imp.str(info.getCentralContactPoint());
		}
		actor.email = info.getEmail();
		actor.telefax = info.getTelefax();
		actor.telephone = info.getTelephone();
		actor.website = info.getWebSite();
	}

	private void setVersionTime() {
		String v = ds.getVersion();
		actor.version = Version.fromString(v).getValue();
		var entry = Contacts.getDataEntry(ds);
		if (entry.getTimeStamp() != null) {
			actor.lastChange = entry.getTimeStamp()
					.toGregorianCalendar()
					.getTimeInMillis();
		}
	}
}
