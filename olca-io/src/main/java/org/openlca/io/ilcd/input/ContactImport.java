package org.openlca.io.ilcd.input;

import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.ilcd.contacts.Contact;
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
		return actor != null
				? actor
				: imp.getFromStore(Contact.class, id)
				.map(ds -> new ContactImport(imp, ds).run())
				.orElse(null);
	}

	private Actor createNew() {
		actor = new Actor();
		actor.category = imp.syncCategory(ds, ModelType.ACTOR);
		setDescriptionAttributes();
		Import.mapVersionInfo(ds, actor);
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
}
