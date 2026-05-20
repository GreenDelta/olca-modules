package org.openlca.io.ecospold1.output;

import java.util.Date;

import org.openlca.commons.Strings;
import org.openlca.ecospold.model.DataSet;
import org.openlca.ecospold.model.IEcoSpoldFactory;
import org.openlca.ecospold.model.IExchange;
import org.openlca.ecospold.model.IPerson;
import org.openlca.ecospold.model.IReferenceFunction;
import org.openlca.ecospold.model.ISource;
import org.openlca.ecospold.model.ITimePeriod;
import org.openlca.ecospold.model.impact.ImpactMethodFactory;
import org.openlca.io.Xml;

/**
 * Adds defaults for required structure elements that are missing in a data set.
 */
final class SchemaDefaults {

	private final DataSet ds;
	private final IEcoSpoldFactory factory;

	private SchemaDefaults(DataSet ds) {
		this.ds = ds;
		this.factory = ds.factory();
	}

	static void write(DataSet ds) {
		new SchemaDefaults(ds).write();
	}

	private void write() {
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

	}

	private void checkDataSetAttributes() {
		var r = ds.root();
		if (Strings.isBlank(r.getGenerator())) {
			r.setGenerator("openLCA");
		}
		if (r.getNumber() <= 0) {
			r.setNumber(1);
		}
		if (r.getTimestamp() == null) {
			r.setTimestamp(Xml.calendar(new Date()));
		}
	}

	private void checkDataSetInformation() {
		var info = ds.getDataSetInformation();
		if (info == null) {
			info = ds.withDataSetInformation();
			info.setType(defaultDataSetType());
			info.setImpactAssessmentResult(isImpactDataSet());
			info.setVersion(1.0f);
			info.setInternalVersion(1.0f);
			info.setEnergyValues(0);
		}
		if (info.getTimestamp() == null) {
			info.setTimestamp(ds.root().getTimestamp());
		}
		if (info.getLanguageCode() == null) {
			info.setLanguageCode(factory.getLanguageCode("en"));
		}
		if (info.getLocalLanguageCode() == null) {
			var lang = info.getLanguageCode();
			info.setLocalLanguageCode(
				lang != null ? lang : factory.getLanguageCode("en"));
		}
	}

	private void checkValidation() {
		var v = ds.getValidation();
		if (v == null) return;
		if (v.getProofReadingValidator() == 0) {
			var person = addDefaultPerson();
			v.setProofReadingValidator(person.getNumber());
		}
		if (Strings.isBlank(v.getProofReadingDetails())) {
			v.setProofReadingDetails("none");
		}
	}

	private void checkPublication() {
		var pub = ds.withDataGeneratorAndPublication();
		if (pub.getPerson() == 0) {
			var person = addDefaultPerson();
			pub.setPerson(person.getNumber());
		}
		pub.setDataPublishedIn(pub.getDataPublishedIn());
	}

	private void checkDataEntry() {
		var entry = ds.withDataEntryBy();
		if (entry.getPerson() == 0) {
			var person = addDefaultPerson();
			entry.setPerson(person.getNumber());
		}
		if (entry.getQualityNetwork() == null) {
			entry.setQualityNetwork(1L);
		}
	}

	private void checkGeography() {
		var geo = ds.withGeography();
		if (Strings.isBlank(geo.getLocation())) {
			geo.setLocation("GLO");
		}
	}

	private void checkTechnology() {
		if (isImpactDataSet())
			return;
		var tech = ds.withTechnology();
		if (Strings.isBlank(tech.getText())) {
			tech.setText("unspecified");
		}
	}

	private void checkTimePeriod() {
		var time = ds.withTimePeriod();
		if (time.getStartDate() == null) {
			time.setStartDate(Xml.calendar(new Date(253370761200000L)));
		}
		if (time.getEndDate() == null) {
			time.setEndDate(Xml.calendar(new Date(253402210800000L)));
		}
	}

	private void checkReferenceFunction() {
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
		for (ISource source : ds.getSources()) {
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

		if (ds.getSources().isEmpty()) {
			addDefaultSource();
		}
	}

	private void addDefaultSource() {
		var s = factory.createSource();
		s.setNumber(1);
		s.setFirstAuthor("default");
		s.setYear(Util.toXml((short) 9999));
		s.setTitle("Created for EcoSpold 1 compatibility");
		s.setPlaceOfPublications("none");
		s.setSourceType(0);
		ds.withSources().add(s);
	}

	private IPerson addDefaultPerson() {
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
		return factory instanceof ImpactMethodFactory;
	}

	private int defaultDataSetType() {
		if (isImpactDataSet())
			return 4;
		for (IExchange exchange : ds.getExchanges()) {
			if (Integer.valueOf(2).equals(exchange.getOutputGroup()))
				return 5;
		}
		return 1;
	}

	private boolean isReferenceProduct(IExchange exchange) {
		return Integer.valueOf(0).equals(exchange.getOutputGroup());
	}


}
