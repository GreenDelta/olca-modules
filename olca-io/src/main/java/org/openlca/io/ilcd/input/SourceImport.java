package org.openlca.io.ilcd.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FileStore;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.SourceBag;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

public class SourceImport {

	private final ImportConfig config;
	private SourceBag ilcdSource;
	private Source source;

	public SourceImport(ImportConfig config) {
		this.config = config;
	}

	public Source run(org.openlca.ilcd.sources.Source dataSet) {
		this.ilcdSource = new SourceBag(dataSet, config.langOrder());
		var source = config.db().get(Source.class, dataSet.getUUID());
		return source != null
			? source
			: createNew();
	}

	public static Source get(ImportConfig config, String sourceId) {
		var source = config.db().get(Source.class, sourceId);
		if (source != null)
			return source;
		var dataSet = config.store().get(
			org.openlca.ilcd.sources.Source.class, sourceId);
		if (dataSet == null) {
			config.log().error("invalid reference in ILCD data set:" +
				" source '" + sourceId + "' does not exist");
			return null;
		}
		return new SourceImport(config).run(dataSet);
	}

	private Source createNew() {
		source = new Source();
		String[] path = Categories.getPath(ilcdSource.getValue());
		source.category = new CategoryDao(config.db())
				.sync(ModelType.SOURCE, path);
		setDescriptionAttributes();
		importExternalFile();
		return config.insert(source);
	}

	private void setDescriptionAttributes() {
		source.refId = ilcdSource.getId();
		source.name = ilcdSource.getShortName();
		source.description = ilcdSource.getComment();
		source.textReference = ilcdSource.getSourceCitation();
		String v = ilcdSource.getVersion();
		source.version = Version.fromString(v).getValue();
		Date time = ilcdSource.getTimeStamp();
		if (time != null)
			source.lastChange = time.getTime();
	}

	private void importExternalFile() {
		List<String> uris = ilcdSource.getExternalFileURIs();
		File dbDir = config.db().getFileStorageLocation();
		if (uris.isEmpty() || dbDir == null)
			return;
		String uri = uris.get(0);
		try {
			copyFile(dbDir, uri);
		} catch (Exception e) {
			config.log().warn("failed to import external file "
				+ uri + ": " + e.getMessage());
		}
	}

	private void copyFile(File dbDir, String uri) throws Exception {
		String fileName = new File(uri).getName();
		String path = FileStore.getPath(ModelType.SOURCE, source.refId);
		File docDir = new File(dbDir, path);
		if (!docDir.exists()) {
			if (!docDir.mkdirs()) {
				throw new IOException("failed to create " + docDir);
			}
		}
		File dbFile = new File(docDir, fileName);
		if (dbFile.exists())
			return;
		try (InputStream in = config.store().getExternalDocument(
				ilcdSource.getId(), fileName)) {
			if (in == null)
				return;
			Files.copy(in, dbFile.toPath());
			source.externalFile = fileName;
		}
	}

}
