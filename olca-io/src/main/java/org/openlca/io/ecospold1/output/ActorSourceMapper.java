package org.openlca.io.ecospold1.output;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Source;
import org.openlca.ecospold.IEcoSpoldFactory;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.io.DataSet;

final class ActorSourceMapper {

	private final IEcoSpoldFactory factory;

	ActorSourceMapper(IEcoSpoldFactory factory) {
		this.factory = factory;
	}

	int map(Actor actor, DataSet ds) {
		if (actor == null)
			return -1;
		int id = (int) actor.id;
		for (IPerson p : ds.getPersons()) {
			if (p.getNumber() == id)
				return id;
		}
		var p = factory.createPerson();
		p.setNumber(id);
		p.setName(actor.name);
		p.setAddress(actor.address);
		p.setCountryCode(factory.getCountryCode(actor.country));
		p.setEmail(actor.email);
		p.setTelefax(actor.telefax);
		p.setTelephone(actor.telephone);
		ds.getPersons().add(p);
		return id;
	}

	int map(Source inSource, DataSet ds) {
		if (inSource == null)
			return -1;
		int id = (int) inSource.id;
		for (ISource s : ds.getSources()) {
			if (s.getNumber() == id)
				return id;
		}
		var s = factory.createSource();
		s.setNumber(id);
		s.setFirstAuthor(inSource.name);
		s.setText(inSource.description);
		s.setTitle(inSource.textReference);
		s.setYear(Util.toXml(inSource.year));
		s.setPlaceOfPublications("unknown");
		s.setSourceType(0);
		ds.getSources().add(s);
		return id;
	}
}
