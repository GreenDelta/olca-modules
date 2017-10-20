package org.openlca.io.ecospold2.input;

import java.io.File;
import java.util.Calendar;

import org.openlca.core.database.ActorDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spold2.Person;
import spold2.PersonList;

/**
 * Updates *existing* contact data sets that are created during a process import
 * with the contact information from a EcoSpold 02 master data file.
 */
public class PersonUpdate implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ActorDao dao;
	private File personFile;

	public PersonUpdate(IDatabase database, File personFile) {
		this.dao = new ActorDao(database);
		this.personFile = personFile;
	}

	@Override
	public void run() {
		log.trace("update actors from {}", personFile);
		try {
			PersonList personList = spold2.IO.read(personFile, PersonList.class);
			if (personList == null)
				return;
			for (Person person : personList.persons) {
				Actor actor = dao.getForRefId(person.id);
				if (actor == null)
					continue;
				updateActor(actor, person);
			}
		} catch (Exception e) {
			log.error("failed to import persons from " + personFile, e);
		}
	}

	private void updateActor(Actor actor, Person person) {
		actor.setName(person.name);
		actor.setAddress(person.address);
		actor.setEmail(person.email);
		actor.setTelefax(person.telefax);
		actor.setTelephone(person.telephone);
		if (person.company != null)
			actor.setDescription("company: " + person.company);
		actor.setLastChange(Calendar.getInstance().getTimeInMillis());
		Version.incUpdate(actor);
		dao.update(actor);
	}
}
