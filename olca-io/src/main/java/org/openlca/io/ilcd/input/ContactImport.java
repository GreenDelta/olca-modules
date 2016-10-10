package org.openlca.io.ilcd.input;

import java.util.Date;

import org.openlca.core.database.ActorDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.util.ContactBag;

public class ContactImport {

	private final ImportConfig config;
	private ActorDao dao;
	private ContactBag ilcdContact;
	private Actor actor;

	public ContactImport(ImportConfig config) {
		this.config = config;
		this.dao = new ActorDao(config.db);
	}

	public Actor run(Contact contact) throws ImportException {
		this.ilcdContact = new ContactBag(contact, config.langs);
		Actor actor = findExisting(ilcdContact.getId());
		if (actor != null)
			return actor;
		return createNew();
	}

	public Actor run(String contactId) throws ImportException {
		Actor actor = findExisting(contactId);
		if (actor != null)
			return actor;
		Contact contact = tryGetContact(contactId);
		ilcdContact = new ContactBag(contact, config.langs);
		return createNew();
	}

	private Actor findExisting(String contactId) throws ImportException {
		try {
			return dao.getForRefId(contactId);
		} catch (Exception e) {
			String message = String.format("Search for actor %s failed.",
					contactId);
			throw new ImportException(message, e);
		}
	}

	private Actor createNew() throws ImportException {
		actor = new Actor();
		importAndSetCategory();
		setDescriptionAttributes();
		setVersionTime();
		saveInDatabase();
		return actor;
	}

	private Contact tryGetContact(String contactId) throws ImportException {
		try {
			Contact contact = config.store.get(Contact.class, contactId);
			if (contact == null) {
				throw new ImportException("No ILCD contact for ID " + contactId
						+ " found");
			}
			return contact;
		} catch (Exception e) {
			throw new ImportException(e.getMessage(), e);
		}
	}

	private void importAndSetCategory() throws ImportException {
		CategoryImport categoryImport = new CategoryImport(config,
				ModelType.ACTOR);
		Category category = categoryImport.run(ilcdContact.getSortedClasses());
		actor.setCategory(category);
	}

	private void setDescriptionAttributes() {
		actor.setRefId(ilcdContact.getId());
		actor.setName(ilcdContact.getName());
		actor.setDescription(ilcdContact.getComment());
		actor.setAddress(ilcdContact.getContactAddress());
		if (actor.getAddress() == null) {
			actor.setAddress(ilcdContact.getCentralContactPoint());
		}
		actor.setEmail(ilcdContact.getEmail());
		actor.setTelefax(ilcdContact.getTelefax());
		actor.setTelephone(ilcdContact.getTelephone());
		actor.setWebsite(ilcdContact.getWebSite());
	}

	private void setVersionTime() {
		String v = ilcdContact.getVersion();
		actor.setVersion(Version.fromString(v).getValue());
		Date time = ilcdContact.getTimeStamp();
		if (time != null)
			actor.setLastChange(time.getTime());
	}

	private void saveInDatabase() throws ImportException {
		try {
			dao.insert(actor);
		} catch (Exception e) {
			String message = String.format("Cannot save actor %s in database.",
					actor.getRefId());
			throw new ImportException(message, e);
		}
	}

}
