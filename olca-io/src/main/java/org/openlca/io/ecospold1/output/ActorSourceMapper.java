package org.openlca.io.ecospold1.output;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Source;
import org.openlca.ecospold.IEcoSpoldFactory;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.io.DataSet;
import org.openlca.io.ecospold1.output.EcoSpold1Export.EcoSpold1Config;

final class ActorSourceMapper {

	private final EcoSpold1Config config;
	private final IEcoSpoldFactory factory;

	ActorSourceMapper(IEcoSpoldFactory factory, EcoSpold1Config config) {
		this.factory = factory;
		this.config = config;
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
		p.setCompanyCode("unknown");
		p.setName(actor.name);
		p.setAddress(actor.address);
		p.setCountryCode(factory.getCountryCode(actor.country));
		p.setEmail(actor.email);
		p.setTelefax(actor.telefax);
		p.setTelephone(actor.telephone);
		ds.getPersons().add(p);
		createDefaults(p);
		return id;
	}

	private void createDefaults(IPerson person) {
		if (config.withDefaults)
			return;
		if (person.getAddress() == null)
			person.setAddress("no address");
		if (person.getTelephone() == null)
			person.setTelephone("000");
		if (person.getCountryCode() == null)
			person.setCountryCode(factory.getCountryCode("CH"));
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
		createDefaults(s);
		return id;
	}

	private void createDefaults(ISource source) {
		if (config.withDefaults)
			return;
		if (source.getTitle() == null)
			source.setTitle("no title");
		if (source.getYear() == null)
			source.setYear(Util.toXml((short) 9999));
	}

}
