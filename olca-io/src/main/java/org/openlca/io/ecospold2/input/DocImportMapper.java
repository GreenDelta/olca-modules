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
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spold2.AdminInfo;
import spold2.DataEntry;
import spold2.DataGenerator;
import spold2.DataSet;
import spold2.FileAttributes;
import spold2.Geography;
import spold2.Representativeness;
import spold2.RichText;
import spold2.Spold2;
import spold2.Technology;
import spold2.Time;

/**
 * Maps the process documentation from an EcoSpold 02 data set to an openLCA
 * data set.
 */
class DocImportMapper {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	private Process process;
	private ProcessDocumentation doc;

	public DocImportMapper(IDatabase database) {
		this.database = database;
	}

	public void map(DataSet ds, Process process) {
		if (ds == null || process == null)
			return;
		this.process = process;
		this.doc = new ProcessDocumentation();
		process.setDocumentation(doc);
		mapTechnology(ds);
		mapGeography(Spold2.getGeography(ds));
		mapTime(Spold2.getTime(ds));
		mapAdminInfo(ds.adminInfo);
		mapRepresentativeness(Spold2.getRepresentativeness(ds));
	}

	private void mapRepresentativeness(Representativeness repri) {
		if (repri == null)
			return;
		doc.setDataTreatment(repri.extrapolations);
		doc.setSampling(repri.samplingProcedure);
	}

	private void mapTechnology(DataSet ds) {
		Technology t = Spold2.getTechnology(ds);
		if (t == null)
			return;
		doc.setTechnology(RichText.join(t.comment));
	}

	private void mapGeography(Geography geography) {
		if (geography == null)
			return;
		process.getDocumentation().setGeography(RichText.join(geography.comment));
		try {
			String refId = KeyGen.get(geography.shortName);
			LocationDao dao = new LocationDao(database);
			Location location = dao.getForRefId(refId);
			process.setLocation(location);
		} catch (Exception e) {
			log.error("failed to load geography from DB", e);
		}
	}

	private void mapTime(Time t) {
		if (t == null)
			return;
		doc.setValidFrom(t.start);
		doc.setValidUntil(t.end);
		doc.setTime(RichText.join(t.comment));
	}

	private void mapAdminInfo(AdminInfo adminInfo) {
		if (adminInfo == null)
			return;
		mapDataEntryBy(adminInfo);
		mapDataGenerator(adminInfo);
		mapPublicationSource(adminInfo);
		mapFileAttributes(adminInfo);
		if (adminInfo.dataGenerator != null)
			doc.setCopyright(adminInfo.dataGenerator.isCopyrightProtected);
	}

	private void mapDataEntryBy(AdminInfo adminInfo) {
		DataEntry dataEntry = adminInfo.dataEntry;
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

	private void mapDataGenerator(AdminInfo adminInfo) {
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

	private void mapPublicationSource(AdminInfo adminInfo) {
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

	private void mapFileAttributes(AdminInfo adminInfo) {
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
