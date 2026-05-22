package org.openlca.io.ecospold1.output;

import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.openlca.commons.Strings;
import org.openlca.ecospold.model.DataSet;
import org.openlca.ecospold.model.IEcoSpoldFactory;
import org.openlca.ecospold.model.IExchange;
import org.openlca.ecospold.model.IPerson;
import org.openlca.ecospold.model.IReferenceFunction;
import org.openlca.ecospold.model.ISource;
import org.openlca.ecospold.model.impact.ImpactMethodFactory;
import org.openlca.io.Xml;

/// Adds defaults for required structure elements that are missing in a data set.
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
		defaultWith(r::getGenerator, r::setGenerator, "openLCA");
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
		defaultWith(v::getProofReadingDetails, v::setProofReadingDetails, "none");
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
		defaultWith(geo::getLocation, geo::setLocation, "GLO");
	}

	private void checkTechnology() {
		if (isImpactDataSet())
			return;
		var tech = ds.withTechnology();
		defaultWith(tech::getText, tech::setText, "unspecified");
	}

	private void checkTimePeriod() {
		var t = ds.withTimePeriod();
		if (t.getStartDate() == null
			&& t.getStartYear() == null
			&& t.getStartYearMonth() == null) {
			t.setStartYear(Xml.calendar());
			t.setStartDate(Xml.calendar(new Date(253370761200000L)));
		}
		if (t.getEndDate() == null) {
			t.setEndDate(Xml.calendar(new Date(253402210800000L)));
		}
	}

	private void checkReferenceFunction() {
		var refFun = ds.withReferenceFunction();
		refFun.setDatasetRelatesToProduct(!isImpactDataSet());
		defaultWith(refFun::getName, refFun::setName, "unspecified");
		defaultWith(refFun::getLocalName, refFun::setLocalName, refFun.getName());
		if (refFun.getAmount() == 0) {
			refFun.setAmount(1.0);
		}
		defaultWith(refFun::getUnit, refFun::setUnit, "unspecified");
		defaultCategories(refFun);
	}

	private void checkExchanges() {
		var nextNumber = 1;
		for (var e : ds.getExchanges()) {
			if (e.getNumber() <= 0) {
				e.setNumber(nextExchangeNumber(nextNumber));
			}
			nextNumber = Math.max(nextNumber, e.getNumber() + 1);
			defaultWith(e::getName, e::setName, "unspecified");
			defaultWith(e::getUnit, e::setUnit, "unspecified");
			defaultCategories(e);
			if (!e.isElementaryFlow()) {
				defaultWith(e::getLocation, e::setLocation, "GLO");
			}
			if (e.isInfrastructureProcess() == null) {
				e.setInfrastructureProcess(false);
			}
			if (e.getUncertaintyType() == null && !isReferenceProduct(e)) {
				e.setUncertaintyType(0);
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
			defaultWith(person::getName, person::setName, "default");
			defaultWith(person::getAddress, person::setAddress, "no address");
			defaultWith(person::getTelephone, person::setTelephone, "000");
			defaultWith(person::getCompanyCode, person::setCompanyCode, "default");
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
			defaultWith(source::getFirstAuthor, source::setFirstAuthor, "default");
			defaultWith(source::getPlaceOfPublications, source::setPlaceOfPublications, "none");
			defaultWith(source::getTitle, source::setTitle, "no title");
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
		for (IPerson person : ds.getPersons()) {
			if (person.getNumber() == 1)
				return person;
		}
		var p = factory.createPerson();
		p.setNumber(1);
		p.setName("default");
		p.setAddress("Created for EcoSpold 1 compatibility");
		p.setTelephone("000");
		p.setCompanyCode("default");
		p.setCountryCode(factory.getCountryCode("CH"));
		ds.getPersons().add(p);
		return p;
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

	private void defaultCategories(IExchange e) {
		defaultWith(e::getCategory, e::setCategory, "unspecified");

		if (e.getLocalCategory() == null) {
			e.setLocalCategory(e.getCategory());
		}
		if (e.getSubCategory() == null) {
			e.setSubCategory("unspecified");
		}
		if (e.getLocalSubCategory() == null) {
			e.setLocalSubCategory(e.getSubCategory());
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


	private void defaultWith(
		Supplier<String> get, Consumer<String> set, String value
	) {
		if (Strings.isBlank(get.get())) {
			set.accept(value);
		}
	}

}
