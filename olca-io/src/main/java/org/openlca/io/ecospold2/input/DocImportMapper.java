package org.openlca.io.ecospold2.input;

import org.openlca.core.database.ActorDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.SourceDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.openlca.ecospold2.Activity;
import org.openlca.ecospold2.AdministrativeInformation;
import org.openlca.ecospold2.DataEntryBy;
import org.openlca.ecospold2.DataGenerator;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.FileAttributes;
import org.openlca.ecospold2.Geography;
import org.openlca.ecospold2.Representativeness;
import org.openlca.ecospold2.Technology;
import org.openlca.ecospold2.TimePeriod;
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps the process documentation from an EcoSpold 02 data set to an openLCA
 * data set.
 */
class DocImportMapper {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	private Process process;
	private ProcessDocumentation doc;
	private DataSet dataSet;

	public DocImportMapper(IDatabase database) {
		this.database = database;
	}

	public void map(DataSet dataSet, Process process) {
		if (dataSet == null || process == null)
			return;
		this.dataSet = dataSet;
		this.process = process;
		this.doc = new ProcessDocumentation();
		process.setDocumentation(doc);
		mapTechnology(dataSet);
		mapGeography(dataSet.geography);
		mapTime(dataSet.timePeriod);
		mapAdminInfo(dataSet.administrativeInformation);
		mapRepresentativeness(dataSet.representativeness);
	}

	private void mapRepresentativeness(Representativeness repri) {
		if (repri == null)
			return;
		doc.setDataTreatment(repri.extrapolations);
		doc.setSampling(repri.samplingProcedure);
	}

	private void mapTechnology(DataSet dataSet) {
		Activity activity = dataSet.activity;
		Technology technology = dataSet.technology;
		if (activity == null || technology == null)
			return;
		doc.setTechnology(technology.comment);
	}

	private void mapGeography(Geography geography) {
		if (geography == null)
			return;
		process.getDocumentation().setGeography(geography.comment);
		try {
			String refId = KeyGen.get(geography.shortName);
			LocationDao dao = new LocationDao(database);
			Location location = dao.getForRefId(refId);
			process.setLocation(location);
		} catch (Exception e) {
			log.error("failed to load geography from DB", e);
		}
	}

	private void mapTime(TimePeriod timePeriod) {
		if (timePeriod == null)
			return;
		doc.setValidFrom(timePeriod.startDate);
		doc.setValidUntil(timePeriod.endDate);
		doc.setTime(timePeriod.comment);
	}

	private void mapAdminInfo(AdministrativeInformation adminInfo) {
		if (adminInfo == null)
			return;
		mapDataEntryBy(adminInfo);
		mapDataGenerator(adminInfo);
		mapPublicationSource(adminInfo);
		mapFileAttributes(adminInfo);
		if (adminInfo.dataGenerator != null)
			doc.setCopyright(adminInfo.dataGenerator.isCopyrightProtected);
	}

	private void mapDataEntryBy(AdministrativeInformation adminInfo) {
		DataEntryBy dataEntry = adminInfo.dataEntryBy;
		if (dataEntry == null || dataEntry.personId == null)
			return;
		ActorDao dao = new ActorDao(database);
		Actor actor = dao.getForRefId(dataEntry.personId);
		if (actor == null) {
			actor = new Actor();
			actor.setRefId(dataEntry.personId);
			actor.setEmail(dataEntry.personEmail);
			actor.setName(dataEntry.personName);
			actor = dao.insert(actor);
		}
		doc.setDataDocumentor(actor);
	}

	private void mapDataGenerator(AdministrativeInformation adminInfo) {
		DataGenerator dataGenerator = adminInfo.dataGenerator;
		if (dataGenerator == null || dataGenerator.personId == null)
			return;
		ActorDao dao = new ActorDao(database);
		Actor actor = dao.getForRefId(dataGenerator.personId);
		if (actor == null) {
			actor = new Actor();
			actor.setRefId(dataGenerator.personId);
			actor.setEmail(dataGenerator.personEmail);
			actor.setName(dataGenerator.personName);
			actor = dao.insert(actor);
		}
		doc.setDataGenerator(actor);
	}

	private void mapPublicationSource(AdministrativeInformation adminInfo) {
		DataGenerator gen = adminInfo.dataGenerator;
		if (gen == null || gen.publishedSourceId == null)
			return;
		SourceDao dao = new SourceDao(database);
		Source source = dao.getForRefId(gen.publishedSourceId);
		if (source == null) {
			source = new Source();
			source.setRefId(gen.publishedSourceId);
			StringBuilder title = new StringBuilder();
			StringBuilder shortTitle = new StringBuilder();
			if (gen.publishedSourceFirstAuthor != null) {
				title.append(gen.publishedSourceFirstAuthor);
				shortTitle.append(gen.publishedSourceFirstAuthor);
			}
			if (gen.publishedSourceYear != null) {
				title.append(gen.publishedSourceYear);
				shortTitle.append(gen.publishedSourceYear);
			}
			source.setTextReference(title.toString());
			source.setName(shortTitle.toString());
			source = dao.insert(source);
		}
		doc.setPublication(source);
	}

	private void mapFileAttributes(AdministrativeInformation adminInfo) {
		if (adminInfo.fileAttributes == null)
			return;
		FileAttributes fileAtts = adminInfo.fileAttributes;
		doc.setCreationDate(fileAtts.creationTimestamp);
		if (fileAtts.lastEditTimestamp != null)
			process.setLastChange(fileAtts.lastEditTimestamp.getTime());
		Version version = new Version(fileAtts.majorRelease,
				fileAtts.majorRevision, fileAtts.minorRelease);
		process.setVersion(version.getValue());
	}

}
