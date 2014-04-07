package org.openlca.io.ecospold2.output;

import java.util.Date;
import java.util.Objects;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Version;
import org.openlca.ecospold2.AdministrativeInformation;
import org.openlca.ecospold2.Company;
import org.openlca.ecospold2.DataEntryBy;
import org.openlca.ecospold2.DataGenerator;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.FileAttributes;
import org.openlca.ecospold2.MacroEconomicScenario;
import org.openlca.ecospold2.Person;
import org.openlca.ecospold2.Representativeness;
import org.openlca.ecospold2.Technology;
import org.openlca.ecospold2.TimePeriod;

class ProcessDoc {

	private final Process process;
	private final ProcessDocumentation doc;
	private final DataSet dataSet;

	private ProcessDoc(Process process, DataSet dataSet) {
		this.process = process;
		this.doc = process.getDocumentation();
		this.dataSet = dataSet;
	}

	public static void map(Process process, DataSet dataSet) {
		if (process == null || process.getDocumentation() == null
				|| dataSet == null)
			return;
		new ProcessDoc(process, dataSet).map();
	}

	private void map() {
		mapTime();
		mapTechnology();
		addEconomicScenario();
		mapRepresentativeness();
		mapAdminInfo();
	}

	private void mapTime() {
		TimePeriod timePeriod = new TimePeriod();
		timePeriod.setComment(doc.getTime());
		timePeriod.setDataValid(true);
		if (doc.getValidUntil() != null)
			timePeriod.setEndDate(doc.getValidUntil());
		else
			timePeriod.setEndDate(new Date());
		if (doc.getValidFrom() != null)
			timePeriod.setStartDate(doc.getValidFrom());
		else
			timePeriod.setStartDate(new Date());
		dataSet.setTimePeriod(timePeriod);
	}

	private void mapTechnology() {
		Technology technology = new Technology();
		technology.setComment(doc.getTechnology());
		technology.setTechnologyLevel(0);
		dataSet.setTechnology(technology);
	}

	private void addEconomicScenario() {
		MacroEconomicScenario scenario = new MacroEconomicScenario();
		scenario.setId("d9f57f0a-a01f-42eb-a57b-8f18d6635801");
		scenario.setName("Business-as-Usual");
		dataSet.setMacroEconomicScenario(scenario);
	}

	private void mapRepresentativeness() {
		Representativeness repri = new Representativeness();
		repri.setSystemModelId("06590a66-662a-4885-8494-ad0cf410f956");
		repri.setSystemModelName("Allocation, ecoinvent default");
		repri.setSamplingProcedure(doc.getSampling());
		repri.setExtrapolations(doc.getDataTreatment());
		dataSet.setRepresentativeness(repri);
	}

	private void mapAdminInfo() {
		AdministrativeInformation adminInfo = new AdministrativeInformation();
		dataSet.setAdministrativeInformation(adminInfo);
		mapDataEntry(adminInfo);
		mapDataGenerator(adminInfo);
		mapFileAttributes(adminInfo);
	}

	private void mapDataEntry(AdministrativeInformation adminInfo) {
		DataEntryBy dataEntryBy = new DataEntryBy();
		adminInfo.setDataEntryBy(dataEntryBy);
		Actor dataDocumentor = doc.getDataDocumentor();
		if (dataDocumentor == null) {
			dataEntryBy.setIsActiveAuthor(false);
			dataEntryBy.setPersonEmail("no@email.com");
			dataEntryBy.setPersonId("788d0176-a69c-4de0-a5d3-259866b6b100");
			dataEntryBy.setPersonName("[Current User]");
		} else {
			dataEntryBy.setPersonEmail(dataDocumentor.getEmail());
			dataEntryBy.setPersonId(dataDocumentor.getRefId());
			dataEntryBy.setPersonName(dataDocumentor.getName());
		}
	}

	private void mapDataGenerator(AdministrativeInformation adminInfo) {
		DataGenerator dataGenerator = new DataGenerator();
		adminInfo.setDataGenerator(dataGenerator);
		Actor actor = doc.getDataGenerator();
		if (actor == null) {
			dataGenerator.setPersonEmail("no@email.com");
			dataGenerator.setPersonId("788d0176-a69c-4de0-a5d3-259866b6b100");
			dataGenerator.setPersonName("[Current User]");
		} else {
			Person person = addPerson(actor);
			dataGenerator.setPersonEmail(person.getEmail());
			dataGenerator.setPersonId(person.getId());
			dataGenerator.setPersonName(person.getEmail());
		}
		// TODO: export source information
		// Source source = doc.getPublication();
		// if (source != null) {
		// dataGenerator.setPublishedSourceId(source.getRefId());
		// dataGenerator.setPublishedSourceFirstAuthor(source.getName());
		// if (source.getYear() != null)
		// dataGenerator.setPublishedSourceYear(source.getYear()
		// .intValue());
		// }
		dataGenerator.setCopyrightProtected(doc.isCopyright());
	}

	private void mapFileAttributes(AdministrativeInformation adminInfo) {
		FileAttributes atts = new FileAttributes();
		adminInfo.setFileAttributes(atts);
		mapVersion(atts);
		atts.setDefaultLanguage("en");
		if (doc.getCreationDate() != null)
			atts.setCreationTimestamp(doc.getCreationDate());
		else
			atts.setCreationTimestamp(new Date());
		if (process.getLastChange() != 0)
			atts.setLastEditTimestamp(new Date(process.getLastChange()));
		else
			atts.setLastEditTimestamp(new Date());
		atts.setInternalSchemaVersion("1.0");
		atts.setFileGenerator("openLCA");
		atts.setFileTimestamp(new Date());
	}

	private void mapVersion(FileAttributes atts) {
		Version version = new Version(process.getVersion());
		atts.setMajorRelease(version.getMajor());
		atts.setMajorRevision(version.getMinor());
		atts.setMinorRelease(version.getUpdate());
		atts.setMinorRevision(0);
	}

	private Person addPerson(Actor actor) {
		for (Person person : dataSet.getMasterData().getPersons()) {
			if (Objects.equals(actor.getRefId(), person.getId()))
				return person;
		}
		Person person = new Person();
		person.setId(actor.getRefId());
		person.setName(actor.getName());
		person.setAddress(getAddress(actor));
		String email = actor.getEmail() != null ? actor.getEmail()
				: "no@mail.net";
		person.setEmail(email);
		person.setName(actor.getName());
		person.setTelefax(actor.getTelefax());
		person.setTelephone(actor.getTelephone());
		person.setCompanyId("b35ea934-b41d-4830-b1aa-c7c678270240");
		person.setCompanyName("UKNWN");
		dataSet.getMasterData().getPersons().add(person);
		addDefaultCompany();
		return person;
	}

	private String getAddress(Actor actor) {
		String adress = "";
		if (actor.getAddress() != null)
			adress += actor.getAddress();
		if (actor.getZipCode() != null)
			adress += " " + actor.getZipCode();
		if (actor.getCity() != null)
			adress += " " + actor.getCity();
		if (actor.getCountry() != null)
			adress += " " + actor.getCountry();
		return adress;
	}

	private void addDefaultCompany() {
		String id = "b35ea934-b41d-4830-b1aa-c7c678270240";
		for (Company company : dataSet.getMasterData().getCompanies()) {
			if (Objects.equals(id, company.getId()))
				return;
		}
		Company company = new Company();
		company.setCode("UKNWN");
		company.setComment("This is a default entry as we cannot create persons"
				+ " without company information for the EcoEditor.");
		company.setId(id);
		company.setName("Unknown");
		dataSet.getMasterData().getCompanies().add(company);
	}
}
