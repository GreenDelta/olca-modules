package org.openlca.io.ilcd.input;

import java.io.File;
import java.nio.file.Files;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FileStore;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.Sources;

public class SourceImport {

	private final Import imp;
	private final org.openlca.ilcd.sources.Source ds;
	private Source source;

	public SourceImport(Import imp, org.openlca.ilcd.sources.Source ds) {
		this.imp = imp;
		this.ds = ds;
	}

	public Source run() {
		var source = imp.db().get(Source.class, Sources.getUUID(ds));
		return source != null
				? source
				: createNew();
	}

	public static Source get(Import imp, String id) {
		var source = imp.db().get(Source.class, id);
		return source != null
				? source
				: imp.getFromStore(org.openlca.ilcd.sources.Source.class, id)
				.map(ds -> new SourceImport(imp, ds).run())
				.orElse(null);
	}

	private Source createNew() {
		source = new Source();
		source.category = new CategoryDao(imp.db())
				.sync(ModelType.SOURCE, Categories.getPath(ds));
		Import.mapVersionInfo(ds, source);
		setDescriptionAttributes();
		importExternalFile();
		return imp.insert(source);
	}

	private void setDescriptionAttributes() {
		source.refId = Sources.getUUID(ds);
		var info = Sources.getDataSetInfo(ds);
		if (info != null) {
			source.name = imp.str(info.getName());
			source.description = imp.str(info.getDescription());
			source.textReference = info.getCitation();
		}
	}

	private void importExternalFile() {
		var fileRefs = Sources.getFileRefs(ds);
		File dbDir = imp.db().getFileStorageLocation();
		if (fileRefs.isEmpty() || dbDir == null)
			return;
		var fileRef = fileRefs.get(0);
		if (fileRef == null)
			return;
		var uri = fileRef.getUri();
		try {
			copyFile(dbDir, uri);
		} catch (Exception e) {
			imp.log().warn("failed to import external file "
					+ uri + ": " + e.getMessage());
		}
	}

	private void copyFile(File dbDir, String uri) throws Exception {
		var fileName = new File(uri).getName();
		var path = FileStore.getPath(ModelType.SOURCE, source.refId);
		var docDir = new File(dbDir, path);
		if (!docDir.exists()) {
			Files.createDirectories(docDir.toPath());
		}
		var dbFile = new File(docDir, fileName);
		if (dbFile.exists())
			return;
		try {
			var stream = imp.store().getExternalDocument(Sources.getUUID(ds), fileName);
			if (stream == null)
				return;
			try (stream) {
				Files.copy(stream, dbFile.toPath());
				source.externalFile = fileName;
			}
		} catch (Exception e) {
			imp.log().error("failed to get external document " + uri);
		}
	}

}
