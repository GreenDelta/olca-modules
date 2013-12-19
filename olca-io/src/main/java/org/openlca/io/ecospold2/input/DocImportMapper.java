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
import org.openlca.io.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps the process documentation from an EcoSpold 02 data set to an openLCA
 * data set.
 */
class DocImportMapper {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	public DocImportMapper(IDatabase database) {
		this.database = database;
	}

	public void map(DataSet dataSet, Process process) {
		if (dataSet == null || process == null)
			return;
		ProcessDocumentation doc = new ProcessDocumentation();
		process.setDocumentation(doc);
		mapTechnology(dataSet, doc);
		mapGeography(dataSet.getGeography(), process);
		mapTime(dataSet.getTimePeriod(), doc);
		mapAdminInfo(dataSet.getAdministrativeInformation(), doc);
		mapRepresentativeness(dataSet.getRepresentativeness(), doc);
	}

	private void mapRepresentativeness(Representativeness repri,
	                                   ProcessDocumentation doc) {
		if (repri == null)
			return;
		doc.setDataTreatment(repri.getExtrapolations());
		doc.setSampling(repri.getSamplingProcedure());
	}

	private void mapTechnology(DataSet dataSet, ProcessDocumentation doc) {
		Activity activity = dataSet.getActivity();
		Technology technology = dataSet.getTechnology();
		if (activity == null || technology == null)
			return;
		doc.setTechnology(technology.getComment());
	}

	private void mapGeography(Geography geography, Process process) {
		if (geography == null)
			return;
		process.getDocumentation().setGeography(geography.getComment());
		try {
			String refId = KeyGen.get(geography.getShortName());
			LocationDao dao = new LocationDao(database);
			Location location = dao.getForRefId(refId);
			process.setLocation(location);
		} catch (Exception e) {
			log.error("failed to load geography from DB", e);
		}
	}

	private void mapTime(TimePeriod timePeriod, ProcessDocumentation doc) {
		if (timePeriod == null)
			return;
		doc.setValidFrom(timePeriod.getStartDate());
		doc.setValidUntil(timePeriod.getEndDate());
		doc.setTime(timePeriod.getComment());
	}

	private void mapAdminInfo(AdministrativeInformation adminInfo,
	                          ProcessDocumentation doc) {
		if (adminInfo == null)
			return;
		mapDataEntryBy(adminInfo, doc);
		mapDataGenerator(adminInfo, doc);
		mapPublicationSource(adminInfo, doc);
		mapFileAttributes(adminInfo, doc);
		if (adminInfo.getDataGenerator() != null)
			doc.setCopyright(adminInfo.getDataGenerator().isCopyrightProtected());
	}

	private void mapDataEntryBy(AdministrativeInformation adminInfo,
	                            ProcessDocumentation doc) {
		DataEntryBy dataEntry = adminInfo.getDataEntryBy();
		if (dataEntry == null || dataEntry.getPersonId() == null)
			return;
		ActorDao dao = new ActorDao(database);
		Actor actor = dao.getForRefId(dataEntry.getPersonId());
		if (actor == null) {
			actor = new Actor();
			actor.setRefId(dataEntry.getPersonId());
			actor.setEmail(dataEntry.getPersonEmail());
			actor.setName(dataEntry.getPersonName());
			actor = dao.insert(actor);
		}
		doc.setDataDocumentor(actor);
	}

	private void mapDataGenerator(AdministrativeInformation adminInfo,
	                              ProcessDocumentation doc) {
		DataGenerator dataGenerator = adminInfo.getDataGenerator();
		if (dataGenerator == null || dataGenerator.getPersonId() == null)
			return;
		ActorDao dao = new ActorDao(database);
		Actor actor = dao.getForRefId(dataGenerator.getPersonId());
		if (actor == null) {
			actor = new Actor();
			actor.setRefId(dataGenerator.getPersonId());
			actor.setEmail(dataGenerator.getPersonEmail());
			actor.setName(dataGenerator.getPersonName());
			actor = dao.insert(actor);
		}
		doc.setDataGenerator(actor);
	}

	private void mapPublicationSource(AdministrativeInformation adminInfo,
	                                  ProcessDocumentation doc) {
		DataGenerator gen = adminInfo.getDataGenerator();
		if (gen == null || gen.getPublishedSourceId() == null)
			return;
		SourceDao dao = new SourceDao(database);
		Source source = dao.getForRefId(gen.getPublishedSourceId());
		if (source == null) {
			source = new Source();
			source.setRefId(gen.getPublishedSourceId());
			StringBuilder title = new StringBuilder();
			StringBuilder shortTitle = new StringBuilder();
			if (gen.getPublishedSourceFirstAuthor() != null) {
				title.append(gen.getPublishedSourceFirstAuthor());
				shortTitle.append(gen.getPublishedSourceFirstAuthor());
			}
			if (gen.getPublishedSourceYear() != null) {
				title.append(gen.getPublishedSourceYear());
				shortTitle.append(gen.getPublishedSourceYear());
			}
			source.setTextReference(title.toString());
			source.setName(shortTitle.toString());
			source = dao.insert(source);
		}
		doc.setPublication(source);
	}

	private void mapFileAttributes(AdministrativeInformation adminInfo,
	                               ProcessDocumentation doc) {
		if (adminInfo.getFileAttributes() == null)
			return;
		FileAttributes fileAtts = adminInfo.getFileAttributes();
		doc.setCreationDate(fileAtts.getCreationTimestamp());
		doc.setLastChange(fileAtts.getLastEditTimestamp());
		String version = "" + fileAtts.getMajorRelease() + "." + fileAtts
				.getMajorRevision();
		doc.setVersion(version);
	}


}
