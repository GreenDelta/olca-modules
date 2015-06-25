package org.openlca.io.ilcd.input;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.SourceDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.util.SourceBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceImport {

	private IDatabase database;
	private DataStore dataStore;
	private SourceBag ilcdSource;
	private Source source;

	public SourceImport(DataStore dataStore, IDatabase database) {
		this.database = database;
		this.dataStore = dataStore;
	}

	public Source run(org.openlca.ilcd.sources.Source source)
			throws ImportException {
		this.ilcdSource = new SourceBag(source);
		Source oSource = findExisting(ilcdSource.getId());
		if (oSource != null)
			return oSource;
		return createNew();
	}

	public Source run(String sourceId) throws ImportException {
		Source source = findExisting(sourceId);
		if (source != null)
			return source;
		org.openlca.ilcd.sources.Source iSource = tryGetSource(sourceId);
		ilcdSource = new SourceBag(iSource);
		return createNew();
	}

	private Source findExisting(String sourceId) throws ImportException {
		try {
			SourceDao dao = new SourceDao(database);
			return dao.getForRefId(sourceId);
		} catch (Exception e) {
			String message = String.format("Search for source %s failed.",
					sourceId);
			throw new ImportException(message, e);
		}
	}

	private Source createNew() throws ImportException {
		source = new Source();
		importAndSetCategory();
		setDescriptionAttributes();
		importExternalFile();
		saveInDatabase();
		return source;
	}

	private org.openlca.ilcd.sources.Source tryGetSource(String sourceId)
			throws ImportException {
		try {
			org.openlca.ilcd.sources.Source iSource = dataStore.get(
					org.openlca.ilcd.sources.Source.class, sourceId);
			if (iSource == null) {
				throw new ImportException("No ILCD source for ID " + sourceId
						+ " found");
			}
			return iSource;
		} catch (Exception e) {
			throw new ImportException(e.getMessage(), e);
		}
	}

	private void setDescriptionAttributes() {
		source.setRefId(ilcdSource.getId());
		source.setName(ilcdSource.getShortName());
		source.setDescription(ilcdSource.getComment());
		source.setTextReference(ilcdSource.getSourceCitation());
		String v = ilcdSource.getVersion();
		source.setVersion(Version.fromString(v).getValue());
		Date time = ilcdSource.getTimeStamp();
		if (time != null)
			source.setLastChange(time.getTime());
	}

	private void importAndSetCategory() throws ImportException {
		CategoryImport categoryImport = new CategoryImport(database,
				ModelType.SOURCE);
		Category category = categoryImport.run(ilcdSource.getSortedClasses());
		source.setCategory(category);
	}

	private void importExternalFile() {
		List<String> uris = ilcdSource.getExternalFileURIs();
		File dbDir = database.getFileStorageLocation();
		if (uris.isEmpty() || dbDir == null)
			return;
		String uri = uris.get(0);
		try {
			copyFile(dbDir, uri);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.warn("failed to import external file " + uri, e);
		}
	}

	private void copyFile(File dbDir, String uri) throws Exception {
		String fileName = new File(uri).getName();
		File docDir = new File(dbDir, "external_docs");
		if (!docDir.exists())
			docDir.mkdirs();
		File dbFile = new File(docDir, fileName);
		if (dbFile.exists())
			return;
		try (InputStream in = dataStore.getExternalDocument(
				ilcdSource.getId(), fileName)) {
			if (in == null)
				return;
			Files.copy(in, dbFile.toPath());
			source.setExternalFile(fileName);
		}
	}

	private void saveInDatabase() throws ImportException {
		try {
			database.createDao(Source.class).insert(source);
		} catch (Exception e) {
			String message = String.format(
					"Cannot save source %s in database.", source.getRefId());
			throw new ImportException(message, e);
		}
	}
}
