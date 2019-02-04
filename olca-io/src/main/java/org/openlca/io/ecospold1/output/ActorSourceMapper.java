package org.openlca.io.ecospold1.output;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Source;
import org.openlca.ecospold.IEcoSpoldFactory;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.io.DataSet;

final class ActorSourceMapper {

	private ExportConfig config;
	private IEcoSpoldFactory factory;

	ActorSourceMapper(IEcoSpoldFactory factory, ExportConfig config) {
		this.factory = factory;
		this.config = config;
	}

	int map(Actor actor, DataSet dataset) {
		if (actor == null)
			return -1;
		int id = (int) actor.id;
		for (IPerson p : dataset.getPersons()) {
			if (p.getNumber() == id)
				return id;
		}
		IPerson person = factory.createPerson();
		person.setNumber(id);
		person.setCompanyCode("unknown");
		person.setName(actor.name);
		person.setAddress(actor.address);
		person.setCountryCode(factory.getCountryCode(actor.country));
		person.setEmail(actor.email);
		person.setTelefax(actor.telefax);
		person.setTelephone(actor.telephone);
		dataset.getPersons().add(person);
		createDefaults(person);
		return id;
	}

	private void createDefaults(IPerson person) {
		if (!config.isCreateDefaults())
			return;
		if (person.getAddress() == null)
			person.setAddress("no address");
		if (person.getTelephone() == null)
			person.setTelephone("000");
		if (person.getCountryCode() == null)
			person.setCountryCode(factory.getCountryCode("CH"));
	}

	int map(Source inSource, DataSet dataset) {
		if (inSource == null)
			return -1;
		int id = (int) inSource.id;
		for (ISource s : dataset.getSources()) {
			if (s.getNumber() == id)
				return id;
		}
		ISource source = factory.createSource();
		source.setNumber(id);
		source.setFirstAuthor(inSource.name);
		source.setText(inSource.description);
		source.setTitle(inSource.textReference);
		source.setYear(Util.toXml(inSource.year));
		source.setPlaceOfPublications("unknown");
		source.setSourceType(0);
		dataset.getSources().add(source);
		createDefaults(source);
		return id;
	}

	private void createDefaults(ISource source) {
		if (!config.isCreateDefaults())
			return;
		if (source.getTitle() == null)
			source.setTitle("no title");
		if (source.getYear() == null)
			source.setYear(Util.toXml(new Short((short) 9999)));
	}

}
