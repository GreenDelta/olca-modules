package org.openlca.io.ecospold1.output;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.openlca.ecospold.IDataEntryBy;
import org.openlca.ecospold.IDataGeneratorAndPublication;
import org.openlca.ecospold.IDataSet;
import org.openlca.ecospold.IDataSetInformation;
import org.openlca.ecospold.IEcoSpoldFactory;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IGeography;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.IReferenceFunction;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.ITechnology;
import org.openlca.ecospold.ITimePeriod;
import org.openlca.ecospold.IValidation;
import org.openlca.ecospold.io.DataSet;
import org.openlca.ecospold.io.DataSetType;
import org.openlca.io.Xml;

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
		checkDataSetAttributes();
		checkDataSetInformation();
		checkDataEntry();
		checkPublication();
		checkValidation();
		checkGeography();
		checkTechnology();
		checkTimePeriod();
		checkReferenceFunction();
		checkExchanges();
		checkPersons();
		checkSources();
		if (dataSet.getSources().isEmpty()) {
			defSource();
		}
	}

	private void checkDataSetAttributes() {
		var dataSet = dataSet();
		if (isBlank(dataSet.getGenerator())) {
			dataSet.setGenerator("openLCA");
		}
		if (dataSet.getNumber() <= 0) {
			dataSet.setNumber(1);
		}
		if (dataSet.getTimestamp() == null) {
			dataSet.setTimestamp(Xml.calendar(new Date()));
		}
	}

	private void checkDataSetInformation() {
		var dataSet = dataSet();
		IDataSetInformation info = dataSet.getDataSetInformation();
		if (info == null) {
			info = factory.createDataSetInformation();
			dataSet.setDataSetInformation(info);
			info.setType(defaultDataSetType());
			info.setImpactAssessmentResult(isImpactDataSet());
			info.setVersion(1.0f);
			info.setInternalVersion(1.0f);
			info.setEnergyValues(0);
		}
		if (info.getTimestamp() == null) {
			info.setTimestamp(dataSet.getTimestamp());
		}
		if (info.getLanguageCode() == null) {
			info.setLanguageCode(factory.getLanguageCode("en"));
		}
		if (info.getLocalLanguageCode() == null) {
			var lang = info.getLanguageCode();
			info.setLocalLanguageCode(lang != null ? lang : factory.getLanguageCode("en"));
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
		publication.setDataPublishedIn(publication.getDataPublishedIn());
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
		if (entry.getQualityNetwork() == null) {
			entry.setQualityNetwork(BigInteger.ONE);
		}
	}

	private void checkGeography() {
		var dataSet = dataSet();
		IGeography geography = dataSet.getGeography();
		if (geography == null) {
			geography = factory.createGeography();
			dataSet.setGeography(geography);
		}
		if (geography.getLocation() == null) {
			geography.setLocation("GLO");
		}
	}

	private void checkTechnology() {
		if (isImpactDataSet())
			return;
		var dataSet = dataSet();
		ITechnology technology = dataSet.getTechnology();
		if (technology == null) {
			technology = factory.createTechnology();
			dataSet.setTechnology(technology);
		}
		if (isBlank(technology.getText())) {
			technology.setText("unspecified");
		}
	}

	private void checkTimePeriod() {
		var dataSet = dataSet();
		ITimePeriod time = dataSet.getTimePeriod();
		if (time == null && isImpactDataSet())
			return;
		if (time == null) {
			time = factory.createTimePeriod();
			time.setDataValidForEntirePeriod(true);
			dataSet.setTimePeriod(time);
		}
		if (time.getStartDate() == null) {
			time.setStartDate(Xml.calendar(new Date(253370761200000L)));
		}
		if (time.getEndDate() == null) {
			time.setEndDate(Xml.calendar(new Date(253402210800000L)));
		}
	}

	private void checkReferenceFunction() {
		var dataSet = dataSet();
		IReferenceFunction refFun = dataSet.getReferenceFunction();
		if (refFun == null) {
			refFun = factory.createReferenceFunction();
			dataSet.setReferenceFunction(refFun);
		}
		refFun.setDatasetRelatesToProduct(!isImpactDataSet());
		refFun.setInfrastructureIncluded(refFun.isInfrastructureIncluded());
		if (isBlank(refFun.getName())) {
			refFun.setName("unspecified");
		}
		if (isBlank(refFun.getLocalName())) {
			refFun.setLocalName(refFun.getName());
		}
		if (refFun.getAmount() == 0) {
			refFun.setAmount(1.0);
		}
		if (isBlank(refFun.getUnit())) {
			refFun.setUnit("unspecified");
		}
		defaultCategories(refFun);
	}

	private void checkExchanges() {
		var nextNumber = 1;
		for (IExchange exchange : dataSet().getExchanges()) {
			if (exchange.getNumber() <= 0) {
				exchange.setNumber(nextExchangeNumber(nextNumber));
			}
			nextNumber = Math.max(nextNumber, exchange.getNumber() + 1);
			if (isBlank(exchange.getName())) {
				exchange.setName("unspecified");
			}
			if (isBlank(exchange.getUnit())) {
				exchange.setUnit("unspecified");
			}
			defaultCategories(exchange);
			if (!exchange.isElementaryFlow() && isBlank(exchange.getLocation())) {
				exchange.setLocation("GLO");
			}
			if (exchange.isInfrastructureProcess() == null) {
				exchange.setInfrastructureProcess(false);
			}
			if (exchange.getUncertaintyType() == null && !isReferenceProduct(exchange)) {
				exchange.setUncertaintyType(0);
			}
		}
	}

	private void checkPersons() {
		var nextNumber = 1;
		for (IPerson person : dataSet().getPersons()) {
			if (person.getNumber() <= 0) {
				person.setNumber(nextPersonNumber(nextNumber));
			}
			nextNumber = Math.max(nextNumber, person.getNumber() + 1);
			if (isBlank(person.getName())) {
				person.setName("default");
			}
			if (person.getAddress() == null) {
				person.setAddress("no address");
			}
			if (person.getTelephone() == null) {
				person.setTelephone("000");
			}
			if (isBlank(person.getCompanyCode())) {
				person.setCompanyCode("default");
			}
			if (person.getCountryCode() == null) {
				person.setCountryCode(factory.getCountryCode("CH"));
			}
		}
	}

	private void checkSources() {
		var nextNumber = 1;
		for (ISource source : dataSet().getSources()) {
			if (source.getNumber() <= 0) {
				source.setNumber(nextSourceNumber(nextNumber));
			}
			nextNumber = Math.max(nextNumber, source.getNumber() + 1);
			if (isBlank(source.getFirstAuthor())) {
				source.setFirstAuthor("default");
			}
			if (isBlank(source.getPlaceOfPublications())) {
				source.setPlaceOfPublications("none");
			}
			if (source.getTitle() == null) {
				source.setTitle("no title");
			}
			if (source.getYear() == null) {
				source.setYear(Util.toXml((short) 9999));
			}
			source.setSourceType(source.getSourceType());
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

	private boolean isImpactDataSet() {
		return factory == DataSetType.IMPACT_METHOD.getFactory();
	}

	private int defaultDataSetType() {
		if (isImpactDataSet())
			return 4;
		for (IExchange exchange : dataSet().getExchanges()) {
			if (Integer.valueOf(2).equals(exchange.getOutputGroup()))
				return 5;
		}
		return 1;
	}

	private boolean isReferenceProduct(IExchange exchange) {
		return Integer.valueOf(0).equals(exchange.getOutputGroup());
	}

	private int nextPersonNumber(int fallback) {
		return nextNumber(dataSet().getPersons(), fallback);
	}

	private int nextSourceNumber(int fallback) {
		return nextNumber(dataSet().getSources(), fallback);
	}

	private int nextExchangeNumber(int fallback) {
		return nextNumber(dataSet().getExchanges(), fallback);
	}

	private int nextNumber(List<?> values, int fallback) {
		var next = Math.max(1, fallback);
		for (var value : values) {
			int number = switch (value) {
				case IPerson person -> person.getNumber();
				case ISource source -> source.getNumber();
				case IExchange exchange -> exchange.getNumber();
				default -> 0;
			};
			if (number >= next) {
				next = number + 1;
			}
		}
		return next;
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
