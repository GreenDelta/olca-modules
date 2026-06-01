package org.openlca.io.ecospold1.output;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

import org.openlca.commons.Strings;
import org.openlca.ecospold.model.DataSet;
import org.openlca.ecospold.model.IExchange;
import org.openlca.ecospold.model.IPerson;
import org.openlca.ecospold.model.ISource;
import org.openlca.ecospold.model.impact.ImpactMethodFactory;
import org.openlca.io.Xml;

/// Adds defaults for required structure elements that are missing in a data set.
final class SchemaDefaults {

	private static final Pattern CAS = Pattern.compile("\\d{2,7}-\\d{2}-\\d");

	private final DataSet ds;

	private IPerson defaultPerson;

	private SchemaDefaults(DataSet ds) {
		this.ds = ds;
	}

	static void write(DataSet ds) {
		new SchemaDefaults(ds).write();
	}

	private void write() {
		checkDataSetAttributes();
		checkReferenceFunction();
		checkGeography();
		checkTechnology();
		checkTimePeriod();
		checkDataSetInformation();
		checkSources();
		checkValidation();
		checkDataEntry();
		checkPublication();
		checkPersons();
		checkExchanges();
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

	private void checkReferenceFunction() {
		var refFun = ds.withReferenceFunction();
		refFun.setDatasetRelatesToProduct(!isImpactDataSet());
		defaultWith(refFun::getName, refFun::setName, "unspecified");
		defaultWith(refFun::getLocalName, refFun::setLocalName, refFun.getName());
		defaultWith(refFun::getUnit, refFun::setUnit, "unspecified");
		defaultWith(refFun::getCategory, refFun::setCategory, "unspecified");
		defaultWith(refFun::getLocalCategory, refFun::setLocalCategory,
			refFun.getCategory());
		defaultWith(refFun::getSubCategory, refFun::setSubCategory, "unspecified");
		defaultWith(refFun::getLocalSubCategory, refFun::setLocalSubCategory,
			refFun.getSubCategory());
		if (!isImpactDataSet()) {
			// the field can be null but the getter returns a primitive!
			refFun.setInfrastructureIncluded(refFun.isInfrastructureIncluded());
		}
		refFun.setCASNumber(validCasOf(refFun.getCASNumber()));
	}

	private void checkDataSetInformation() {
		var info = ds.getDataSetInformation();
		if (info == null) {
			info = ds.withDataSetInformation();
			info.setType(defaultDataSetType());
			info.setImpactAssessmentResult(false);
			info.setVersion(1.0f);
			info.setInternalVersion(1.0f);
			info.setEnergyValues(0);
		}
		if (info.getTimestamp() == null) {
			info.setTimestamp(Xml.calendar(new Date()));
		}
		if (info.getLanguageCode() == null) {
			info.setLanguageCode(ds.factory().getLanguageCode("en"));
		}
		if (info.getLocalLanguageCode() == null) {
			info.setLocalLanguageCode(info.getLanguageCode());
		}
	}

	private int defaultDataSetType() {
		if (isImpactDataSet())
			return 4;
		var coProd = Integer.valueOf(2);
		for (IExchange exchange : ds.getExchanges()) {
			if (coProd.equals(exchange.getOutputGroup()))
				return 5;
		}
		return 0;
	}

	private void checkValidation() {
		var v = ds.getValidation();
		if (v == null) return;
		if (v.getProofReadingValidator() == 0) {
			var person = getDefaultPerson();
			v.setProofReadingValidator(person.getNumber());
		}
		defaultWith(v::getProofReadingDetails, v::setProofReadingDetails, "none");
	}

	private void checkPublication() {
		var pub = ds.withDataGeneratorAndPublication();
		if (pub.getPerson() == 0) {
			var person = getDefaultPerson();
			pub.setPerson(person.getNumber());
		}
		// the field can be null but the getter returns a primitive!
		pub.setDataPublishedIn(pub.getDataPublishedIn());
	}

	private void checkDataEntry() {
		var entry = ds.withDataEntryBy();
		if (entry.getPerson() == 0) {
			var person = getDefaultPerson();
			entry.setPerson(person.getNumber());
		}
		if (entry.getQualityNetwork() == null) {
			entry.setQualityNetwork(1L);
		}
	}

	private void checkGeography() {
		var geo = ds.withGeography();
		defaultWith(geo::getLocation, geo::setLocation, "GLO", 7);
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
			t.setStartYear(Util.xmlYear(9999));
		}
		if (t.getEndDate() == null
			&& t.getEndYear() == null
			&& t.getEndYearMonth() == null) {
			t.setEndYear(Util.xmlYear(9999));
		}
	}


	private void checkExchanges() {

		for (var e : ds.getExchanges()) {
			defaultWith(e::getName, e::setName, "unspecified");
			defaultWith(e::getUnit, e::setUnit, "unspecified");
			defaultWith(e::getCategory, e::setCategory, "unspecified");
			defaultWith(e::getLocalCategory, e::setLocalCategory, e.getCategory());
			defaultWith(e::getSubCategory, e::setSubCategory, "unspecified");
			defaultWith(e::getLocalSubCategory, e::setLocalSubCategory,
				e.getSubCategory());

			if (!e.isElementaryFlow()) {
				defaultWith(e::getLocation, e::setLocation, "GLO", 7);
			} else {
				optLen(e::getLocation, e::setLocation, 7);
			}

			if (!e.isElementaryFlow() && e.isInfrastructureProcess() == null) {
				e.setInfrastructureProcess(false);
			}
			e.setCASNumber(validCasOf(e.getCASNumber()));

			// clear uncertainty information for the reference flow
			if (isRefFlow(e)) {
				e.setUncertaintyType(0);
				e.setStandardDeviation95(null);
				e.setMostLikelyValue(null);
				e.setMaxValue(null);
				e.setMinValue(null);
			}
		}
	}

	private boolean isRefFlow(IExchange e) {
		return e != null
			&& e.getOutputGroup() != null
			&& e.getOutputGroup() == 0;
	}

	private void checkPersons() {
		for (var person : ds.getPersons()) {
			defaultWith(person::getName, person::setName, "unknown", 40);
			defaultWith(person::getAddress, person::setAddress, "no address");
			defaultWith(person::getTelephone, person::setTelephone, "000");
			defaultWith(person::getCompanyCode, person::setCompanyCode, "unknown");
			if (person.getCountryCode() == null) {
				person.setCountryCode(ds.factory().getCountryCode("CH"));
			}
		}
	}

	private void checkSources() {
		for (var s : ds.getSources()) {
			defaultWith(s::getFirstAuthor, s::setFirstAuthor, "unknown", 40);
			defaultWith(s::getTitle, s::setTitle, "no title");
			defaultWith(s::getPlaceOfPublications, s::setPlaceOfPublications, "unknown");
			if (s.getYear() == null) {
				s.setYear(Util.xmlYear(9999));
			}
		}
		if (ds.getSources().isEmpty()) {
			addDefaultSource();
		}
	}

	private void addDefaultSource() {
		var s = ds.withSource();
		s.setNumber(nextNumOf(ds.getSources(), ISource::getNumber));
		s.setFirstAuthor("default");
		s.setYear(Util.xmlYear(Calendar.getInstance().get(Calendar.YEAR)));
		s.setTitle("Created for EcoSpold 1 compatibility");
		s.setPlaceOfPublications("none");
		s.setSourceType(0);
	}

	private IPerson getDefaultPerson() {
		if (defaultPerson != null)
			return defaultPerson;
		var p = ds.withPerson();
		p.setNumber(nextNumOf(ds.getPersons(), IPerson::getNumber));
		p.setName("Default");
		p.setAddress("Created for EcoSpold 1 compatibility");
		p.setTelephone("000");
		p.setCompanyCode("default");
		p.setCountryCode(ds.factory().getCountryCode("CH"));
		defaultPerson = p;
		return p;
	}

	private boolean isImpactDataSet() {
		return ds.factory() instanceof ImpactMethodFactory;
	}

	private void defaultWith(
		Supplier<String> get, Consumer<String> set, String value
	) {
		defaultWith(get, set, value, -1);
	}

	private void defaultWith(
		Supplier<String> get, Consumer<String> set, String value, int len
	) {
		var s = get.get();
		if (Strings.isBlank(s)) {
			set.accept(value);
			return;
		}
		if (len > 0 && s.length() > len) {
			set.accept(Strings.cutEnd(s, len));
		}
	}

	private <T> int nextNumOf(List<T> elems, ToIntFunction<T> get) {
		var nextNum = 1;
		for (var e : elems) {
			int num = get.applyAsInt(e);
			if (num >= nextNum) {
				nextNum = num + 1;
			}
		}
		return nextNum;
	}

	private static String validCasOf(String value) {
		if (Strings.isBlank(value))
			return null;
		var cas = value.trim();
		return CAS.matcher(cas).matches()
			? cas
			: null;
	}

	private static void optLen(
		Supplier<String> get, Consumer<String> set, int len
	) {
		var val = get.get();
		if (Strings.isBlank(val))
			return;
		if (val.length() > len) {
			set.accept(Strings.cutEnd(val, len));
		}
	}

}
