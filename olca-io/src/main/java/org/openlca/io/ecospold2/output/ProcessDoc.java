package org.openlca.io.ecospold2.output;

import java.util.Date;
import java.util.Objects;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Version;

import spold2.ActivityDescription;
import spold2.AdminInfo;
import spold2.Company;
import spold2.DataEntry;
import spold2.DataGenerator;
import spold2.DataSet;
import spold2.FileAttributes;
import spold2.MacroEconomicScenario;
import spold2.Person;
import spold2.Representativeness;
import spold2.RichText;
import spold2.Source;
import spold2.Spold2;
import spold2.Technology;
import spold2.Time;

class ProcessDoc {

	private final Process process;
	private final ProcessDocumentation doc;
	private final DataSet dataSet;

	private ProcessDoc(Process process, DataSet dataSet) {
		this.process = process;
		this.doc = process.getDocumentation();
		this.dataSet = dataSet;
		if (dataSet.description == null)
			dataSet.description = new ActivityDescription();
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
		Time timePeriod = new Time();
		timePeriod.comment = RichText.of(doc.getTime());
		timePeriod.dataValid = true;
		if (doc.getValidUntil() != null)
			timePeriod.end = doc.getValidUntil();
		else
			timePeriod.end = new Date();
		if (doc.getValidFrom() != null)
			timePeriod.start = doc.getValidFrom();
		else
			timePeriod.start = new Date();
		dataSet.description.timePeriod = timePeriod;
	}

	private void mapTechnology() {
		Technology tech = new Technology();
		tech.comment = RichText.of(doc.getTechnology());
		tech.level = 0;
		dataSet.description.technology = tech;
	}

	private void addEconomicScenario() {
		MacroEconomicScenario scenario = new MacroEconomicScenario();
		scenario.id = "d9f57f0a-a01f-42eb-a57b-8f18d6635801";
		scenario.name = "Business-as-Usual";
		dataSet.description.macroEconomicScenario = scenario;
	}

	private void mapRepresentativeness() {
		Representativeness repri = Spold2.representativeness(dataSet);
		repri.systemModelId = "06590a66-662a-4885-8494-ad0cf410f956";
		repri.systemModelName = "Allocation, ecoinvent default";
		repri.samplingProcedure = doc.getSampling();
		repri.extrapolations = doc.getDataTreatment();
	}

	private void mapAdminInfo() {
		AdminInfo adminInfo = new AdminInfo();
		dataSet.adminInfo = adminInfo;
		mapDataEntry(adminInfo);
		mapDataGenerator(adminInfo);
		mapFileAttributes(adminInfo);
	}

	private void mapDataEntry(AdminInfo adminInfo) {
		DataEntry dataEntryBy = new DataEntry();
		adminInfo.dataEntry = dataEntryBy;
		Actor dataDocumentor = doc.getDataDocumentor();
		if (dataDocumentor == null) {
			dataEntryBy.isActiveAuthor = false;
			dataEntryBy.personEmail = "no@email.com";
			dataEntryBy.personId = "788d0176-a69c-4de0-a5d3-259866b6b100";
			dataEntryBy.personName = "[Current User]";
		} else {
			dataEntryBy.personEmail = dataDocumentor.getEmail();
			dataEntryBy.personId = dataDocumentor.getRefId();
			dataEntryBy.personName = dataDocumentor.getName();
		}
	}

	private void mapDataGenerator(AdminInfo adminInfo) {
		DataGenerator generator = new DataGenerator();
		adminInfo.dataGenerator = generator;
		generator.isCopyrightProtected = doc.isCopyright();
		mapPublication(generator);
		Actor actor = doc.getDataGenerator();
		if (actor == null) {
			generator.personEmail = "no@email.com";
			generator.personId = "788d0176-a69c-4de0-a5d3-259866b6b100";
			generator.personName = "[Current User]";
		} else {
			Person person = addPerson(actor);
			generator.personEmail = person.email;
			generator.personId = person.id;
			generator.personName = person.email;
		}
	}

	private void mapPublication(DataGenerator generator) {
		if (doc.getPublication() == null)
			return;
		Source source = addSource(doc.getPublication());
		generator.publishedSourceId = source.id;
		generator.publishedSourceFirstAuthor = source.firstAuthor;
		generator.publishedSourceYear = source.year;
	}

	private void mapFileAttributes(AdminInfo adminInfo) {
		FileAttributes atts = new FileAttributes();
		adminInfo.fileAttributes = atts;
		mapVersion(atts);
		atts.defaultLanguage = "en";
		if (doc.getCreationDate() != null)
			atts.creationTimestamp = doc.getCreationDate();
		else
			atts.creationTimestamp = new Date();
		if (process.getLastChange() != 0)
			atts.lastEditTimestamp = new Date(process.getLastChange());
		else
			atts.lastEditTimestamp = new Date();
		atts.internalSchemaVersion = "1.0";
		atts.fileGenerator = "openLCA";
		atts.fileTimestamp = new Date();
	}

	private void mapVersion(FileAttributes atts) {
		Version version = new Version(process.getVersion());
		atts.majorRelease = version.getMajor();
		atts.majorRevision = version.getMinor();
		atts.minorRelease = version.getUpdate();
		atts.minorRevision = 0;
	}

	private Source addSource(org.openlca.core.model.Source olcaSource) {
		for (Source source : dataSet.masterData.sources) {
			if (Objects.equals(olcaSource.getRefId(), source.id))
				return source;
		}
		Source source = new Source();
		source.id = olcaSource.getRefId();
		source.comment = olcaSource.getDescription();
		source.firstAuthor = olcaSource.getName();
		source.sourceType = 0;
		source.title = olcaSource.getTextReference();
		if (olcaSource.getYear() != null)
			source.year = olcaSource.getYear().intValue();
		else
			source.year = 9999;
		dataSet.masterData.sources.add(source);
		return source;
	}

	private Person addPerson(Actor actor) {
		for (Person person : dataSet.masterData.persons) {
			if (Objects.equals(actor.getRefId(), person.id))
				return person;
		}
		Person person = new Person();
		person.id = actor.getRefId();
		person.name = actor.getName();
		person.address = getAddress(actor);
		String email = actor.getEmail() != null ? actor.getEmail()
				: "no@mail.net";
		person.email = email;
		person.name = actor.getName();
		person.telefax = actor.getTelefax();
		person.telephone = actor.getTelephone();
		person.companyId = "b35ea934-b41d-4830-b1aa-c7c678270240";
		person.company = "UKNWN";
		dataSet.masterData.persons.add(person);
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
		for (Company company : dataSet.masterData.companies) {
			if (Objects.equals(id, company.id))
				return;
		}
		Company company = new Company();
		company.code = "UKNWN";
		company.comment = "This is a default entry as we cannot create persons"
				+ " without company information for the EcoEditor.";
		company.id = id;
		company.name = "Unknown";
		dataSet.masterData.companies.add(company);
	}
}
