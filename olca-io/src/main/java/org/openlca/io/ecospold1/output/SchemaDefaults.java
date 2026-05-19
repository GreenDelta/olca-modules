package org.openlca.io.ecospold1.output;

import java.util.Date;

import org.openlca.ecospold.IDataEntryBy;
import org.openlca.ecospold.IDataGeneratorAndPublication;
import org.openlca.ecospold.IDataSet;
import org.openlca.ecospold.IEcoSpoldFactory;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.IReferenceFunction;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.IValidation;
import org.openlca.ecospold.io.DataSet;

/**
 * Adds defaults for required structure elements that are missing in a data set.
 */
final class SchemaDefaults {

	private final IDataSet ds;
	private final IEcoSpoldFactory factory;

	private SchemaDefaults(IDataSet ds, IEcoSpoldFactory factory) {
		this.ds = ds;
		this.factory = factory;
	}

	static void write(IDataSet ds, IEcoSpoldFactory factory) {
		new SchemaDefaults(ds, factory).write();
	}

	private void write() {
		var dataSet = dataSet();
		checkDataEntry();
		checkPublication();
		checkValidation();
		checkGeography();
		checkTimePeriod();
		checkReferenceFunction();
		checkExchanges();
		checkPersons();
		checkSources();
		if (dataSet.getSources().isEmpty()) {
			defSource();
		}
	}

	private void checkValidation() {
		IValidation validation = dataSet().getValidation();
		if (validation == null)
			return;
		if (validation.getProofReadingValidator() == 0) {
			IPerson person = defPerson();
			validation.setProofReadingValidator(person.getNumber());
		}
		if (validation.getProofReadingDetails() == null)
			validation.setProofReadingDetails("none");
	}

	private void checkPublication() {
		var dataSet = dataSet();
		IDataGeneratorAndPublication publication = dataSet
				.getDataGeneratorAndPublication();
		if (publication == null) {
			publication = factory.createDataGeneratorAndPublication();
			dataSet.setDataGeneratorAndPublication(publication);
		}
		if (publication.getPerson() == 0) {
			IPerson person = defPerson();
			publication.setPerson(person.getNumber());
		}
	}

	private void checkDataEntry() {
		var dataSet = dataSet();
		IDataEntryBy entry = dataSet.getDataEntryBy();
		if (entry == null) {
			entry = factory.createDataEntryBy();
			dataSet.setDataEntryBy(entry);
		}
		if (entry.getPerson() == 0) {
			IPerson person = defPerson();
			entry.setPerson(person.getNumber());
		}
	}

	private void checkGeography() {
		var geography = dataSet().getGeography();
		if (geography != null && geography.getLocation() == null) {
			geography.setLocation("GLO");
		}
	}

	private void checkTimePeriod() {
		var time = dataSet().getTimePeriod();
		if (time == null)
			return;
		if (time.getStartDate() == null) {
			time.setStartDate(org.openlca.io.Xml.calendar(new Date(253370761200000L)));
		}
		if (time.getEndDate() == null) {
			time.setEndDate(org.openlca.io.Xml.calendar(new Date(253402210800000L)));
		}
	}

	private void checkReferenceFunction() {
		IReferenceFunction refFun = dataSet().getReferenceFunction();
		if (refFun == null)
			return;
		defaultCategories(refFun);
	}

	private void checkExchanges() {
		for (IExchange exchange : dataSet().getExchanges()) {
			defaultCategories(exchange);
			if (exchange.getOutputGroup() == 0 && exchange.getLocation() == null) {
				exchange.setLocation("GLO");
			}
		}
	}

	private void checkPersons() {
		for (IPerson person : dataSet().getPersons()) {
			if (person.getAddress() == null) {
				person.setAddress("no address");
			}
			if (person.getTelephone() == null) {
				person.setTelephone("000");
			}
			if (person.getCountryCode() == null) {
				person.setCountryCode(factory.getCountryCode("CH"));
			}
		}
	}

	private void checkSources() {
		for (ISource source : dataSet().getSources()) {
			if (source.getTitle() == null) {
				source.setTitle("no title");
			}
			if (source.getYear() == null) {
				source.setYear(Util.toXml((short) 9999));
			}
		}
	}

	private ISource defSource() {
		var dataSet = dataSet();
		for (ISource source : dataSet.getSources()) {
			if (source.getNumber() == 1)
				return source;
		}
		ISource source = factory.createSource();
		source.setNumber(1);
		source.setFirstAuthor("default");
		source.setYear(Util.toXml((short) 9999));
		source.setTitle("Created for EcoSpold 1 compatibility");
		source.setPlaceOfPublications("none");
		source.setSourceType(0);
		dataSet.getSources().add(source);
		return source;
	}

	private IPerson defPerson() {
		var dataSet = dataSet();
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

	private DataSet dataSet() {
		return new DataSet(ds, factory);
	}

	private void defaultCategories(IReferenceFunction refFun) {
		if (refFun.getCategory() == null) {
			refFun.setCategory("unspecified");
		}
		if (refFun.getLocalCategory() == null) {
			refFun.setLocalCategory(refFun.getCategory());
		}
		if (refFun.getSubCategory() == null) {
			refFun.setSubCategory("unspecified");
		}
		if (refFun.getLocalSubCategory() == null) {
			refFun.setLocalSubCategory(refFun.getSubCategory());
		}
	}

	private void defaultCategories(IExchange exchange) {
		if (exchange.getCategory() == null) {
			exchange.setCategory("unspecified");
		}
		if (exchange.getLocalCategory() == null) {
			exchange.setLocalCategory(exchange.getCategory());
		}
		if (exchange.getSubCategory() == null) {
			exchange.setSubCategory("unspecified");
		}
		if (exchange.getLocalSubCategory() == null) {
			exchange.setLocalSubCategory(exchange.getSubCategory());
		}
	}
}
