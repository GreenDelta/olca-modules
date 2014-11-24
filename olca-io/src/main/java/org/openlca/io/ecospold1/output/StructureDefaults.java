package org.openlca.io.ecospold1.output;

import org.openlca.ecospold.IDataEntryBy;
import org.openlca.ecospold.IDataGeneratorAndPublication;
import org.openlca.ecospold.IEcoSpoldFactory;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.IValidation;
import org.openlca.ecospold.io.DataSet;

/**
 * Adds defaults for required structure elements that are missing in a data set.
 */
final class StructureDefaults {

	private StructureDefaults() {
	}

	public static void add(DataSet dataSet, IEcoSpoldFactory factory) {
		checkDataEntry(dataSet, factory);
		checkPublication(dataSet, factory);
		checkValidation(dataSet, factory);
		if (dataSet.getSources().isEmpty())
			defSource(dataSet, factory);
	}

	private static void checkValidation(DataSet dataSet,
			IEcoSpoldFactory factory) {
		IValidation validation = dataSet.getValidation();
		if (validation == null)
			return;
		if (validation.getProofReadingValidator() == 0) {
			IPerson person = defPerson(dataSet, factory);
			validation.setProofReadingValidator(person.getNumber());
		}
		if (validation.getProofReadingDetails() == null)
			validation.setProofReadingDetails("none");
	}

	private static void checkPublication(DataSet dataSet,
			IEcoSpoldFactory factory) {
		IDataGeneratorAndPublication publication = dataSet
				.getDataGeneratorAndPublication();
		if (publication == null) {
			publication = factory.createDataGeneratorAndPublication();
			dataSet.setDataGeneratorAndPublication(publication);
		}
		if (publication.getPerson() == 0) {
			IPerson person = defPerson(dataSet, factory);
			publication.setPerson(person.getNumber());
		}
	}

	private static void checkDataEntry(DataSet dataSet, IEcoSpoldFactory factory) {
		IDataEntryBy entry = dataSet.getDataEntryBy();
		if (entry == null) {
			entry = factory.createDataEntryBy();
			dataSet.setDataEntryBy(entry);
		}
		if (entry.getPerson() == 0) {
			IPerson person = defPerson(dataSet, factory);
			entry.setPerson(person.getNumber());
		}
	}

	private static ISource defSource(DataSet dataSet, IEcoSpoldFactory factory) {
		for (ISource source : dataSet.getSources()) {
			if (source.getNumber() == 1)
				return source;
		}
		ISource source = factory.createSource();
		source.setNumber(1);
		source.setFirstAuthor("default");
		source.setYear(Util.toXml(new Short((short) 9999)));
		source.setTitle("Created for EcoSpold 1 compatibility");
		source.setPlaceOfPublications("none");
		source.setSourceType(0);
		dataSet.getSources().add(source);
		return source;
	}

	private static IPerson defPerson(DataSet dataSet, IEcoSpoldFactory factory) {
		for (IPerson person : dataSet.getPersons()) {
			if (person.getNumber() == 1)
				return person;
		}
		IPerson person = factory.createPerson();
		person.setNumber(1);
		person.setName("default");
		person.setAddress("Created for EcoSpold 1 compatibility");
		person.setTelephone("000");
		person.setCompanyCode("default");
		person.setCountryCode(factory.getCountryCode("CH"));
		dataSet.getPersons().add(person);
		return person;
	}
}
