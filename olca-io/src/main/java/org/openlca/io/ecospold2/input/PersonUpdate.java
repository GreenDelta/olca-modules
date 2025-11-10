package org.openlca.io.ecospold2.input;

import java.io.File;
import java.io.FileInputStream;
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
		try (var stream = new FileInputStream(personFile)) {
			var list = PersonList.readFrom(stream);
			if (list == null)
				return;
			for (Person person : list.persons) {
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
		actor.name = person.name;
		actor.address = person.address;
		actor.email = person.email;
		actor.telefax = person.telefax;
		actor.telephone = person.telephone;
		if (person.company != null)
			actor.description = "company: " + person.company;
		actor.lastChange = Calendar.getInstance().getTimeInMillis();
		Version.incUpdate(actor);
		dao.update(actor);
	}
}
