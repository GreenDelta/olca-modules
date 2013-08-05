package org.openlca.io.ilcd.input;

import org.openlca.core.database.ActorDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.util.ContactBag;

/**
 * The import of an ILCD contact data set to an openLCA database.
 */
public class ContactImport {

	private IDatabase database;
	private ActorDao dao;
	private DataStore dataStore;
	private ContactBag ilcdContact;
	private Actor actor;

	public ContactImport(DataStore dataStore, IDatabase database) {
		this.dataStore = dataStore;
		this.database = database;
		this.dao = new ActorDao(database);
	}

	public Actor run(Contact contact) throws ImportException {
		this.ilcdContact = new ContactBag(contact);
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
		ilcdContact = new ContactBag(contact);
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
		saveInDatabase();
		return actor;
	}

	private Contact tryGetContact(String contactId) throws ImportException {
		try {
			Contact contact = dataStore.get(Contact.class, contactId);
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
		CategoryImport categoryImport = new CategoryImport(database,
				ModelType.ACTOR);
		Category category = categoryImport.run(ilcdContact.getSortedClasses());
		actor.setCategory(category);
	}

	private void setDescriptionAttributes() {
		actor.setRefId(ilcdContact.getId());
		actor.setName(ilcdContact.getName());
		actor.setDescription(ilcdContact.getComment());
		actor.setAddress(ilcdContact.getContactAddress());
		actor.setEmail(ilcdContact.getEmail());
		actor.setTelefax(ilcdContact.getTelefax());
		actor.setTelephone(ilcdContact.getTelephone());
		actor.setWebsite(ilcdContact.getWebSite());
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
