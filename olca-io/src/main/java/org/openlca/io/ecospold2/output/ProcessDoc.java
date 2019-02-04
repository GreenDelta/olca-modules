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
		this.doc = process.documentation;
		this.dataSet = dataSet;
		if (dataSet.description == null)
			dataSet.description = new ActivityDescription();
	}

	public static void map(Process process, DataSet dataSet) {
		if (process == null || process.documentation == null
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
		timePeriod.comment = RichText.of(doc.time);
		timePeriod.dataValid = true;
		if (doc.validUntil != null)
			timePeriod.end = doc.validUntil;
		else
			timePeriod.end = new Date();
		if (doc.validFrom != null)
			timePeriod.start = doc.validFrom;
		else
			timePeriod.start = new Date();
		dataSet.description.timePeriod = timePeriod;
	}

	private void mapTechnology() {
		Technology tech = new Technology();
		tech.comment = RichText.of(doc.technology);
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
		repri.samplingProcedure = doc.sampling;
		repri.extrapolations = doc.dataTreatment;
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
		Actor dataDocumentor = doc.dataDocumentor;
		if (dataDocumentor == null) {
			dataEntryBy.isActiveAuthor = false;
			dataEntryBy.personEmail = "no@email.com";
			dataEntryBy.personId = "788d0176-a69c-4de0-a5d3-259866b6b100";
			dataEntryBy.personName = "[Current User]";
		} else {
			dataEntryBy.personEmail = dataDocumentor.email;
			dataEntryBy.personId = dataDocumentor.refId;
			dataEntryBy.personName = dataDocumentor.name;
		}
	}

	private void mapDataGenerator(AdminInfo adminInfo) {
		DataGenerator generator = new DataGenerator();
		adminInfo.dataGenerator = generator;
		generator.isCopyrightProtected = doc.copyright;
		mapPublication(generator);
		Actor actor = doc.dataGenerator;
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
		if (doc.publication == null)
			return;
		Source source = addSource(doc.publication);
		generator.publishedSourceId = source.id;
		generator.publishedSourceFirstAuthor = source.firstAuthor;
		generator.publishedSourceYear = source.year;
	}

	private void mapFileAttributes(AdminInfo adminInfo) {
		FileAttributes atts = new FileAttributes();
		adminInfo.fileAttributes = atts;
		mapVersion(atts);
		atts.defaultLanguage = "en";
		if (doc.creationDate != null)
			atts.creationTimestamp = doc.creationDate;
		else
			atts.creationTimestamp = new Date();
		if (process.lastChange != 0)
			atts.lastEditTimestamp = new Date(process.lastChange);
		else
			atts.lastEditTimestamp = new Date();
		atts.internalSchemaVersion = "1.0";
		atts.fileGenerator = "openLCA";
		atts.fileTimestamp = new Date();
	}

	private void mapVersion(FileAttributes atts) {
		Version version = new Version(process.version);
		atts.majorRelease = version.getMajor();
		atts.majorRevision = version.getMinor();
		atts.minorRelease = version.getUpdate();
		atts.minorRevision = 0;
	}

	private Source addSource(org.openlca.core.model.Source olcaSource) {
		for (Source source : dataSet.masterData.sources) {
			if (Objects.equals(olcaSource.refId, source.id))
				return source;
		}
		Source source = new Source();
		source.id = olcaSource.refId;
		source.comment = olcaSource.description;
		source.firstAuthor = olcaSource.name;
		source.sourceType = 0;
		source.title = olcaSource.textReference;
		if (olcaSource.year != null)
			source.year = olcaSource.year.intValue();
		else
			source.year = 9999;
		dataSet.masterData.sources.add(source);
		return source;
	}

	private Person addPerson(Actor actor) {
		for (Person person : dataSet.masterData.persons) {
			if (Objects.equals(actor.refId, person.id))
				return person;
		}
		Person person = new Person();
		person.id = actor.refId;
		person.name = actor.name;
		person.address = getAddress(actor);
		String email = actor.email != null ? actor.email
				: "no@mail.net";
		person.email = email;
		person.name = actor.name;
		person.telefax = actor.telefax;
		person.telephone = actor.telephone;
		person.companyId = "b35ea934-b41d-4830-b1aa-c7c678270240";
		person.company = "UKNWN";
		dataSet.masterData.persons.add(person);
		addDefaultCompany();
		return person;
	}

	private String getAddress(Actor actor) {
		String adress = "";
		if (actor.address != null)
			adress += actor.address;
		if (actor.zipCode != null)
			adress += " " + actor.zipCode;
		if (actor.city != null)
			adress += " " + actor.city;
		if (actor.country != null)
			adress += " " + actor.country;
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
