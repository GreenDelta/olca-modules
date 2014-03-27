package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingFileDao;
import org.openlca.core.model.MappingFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MappingFileImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private MappingFileDao sourceDao;
	private MappingFileDao destDao;

	MappingFileImport(IDatabase source, IDatabase dest) {
		sourceDao = new MappingFileDao(source);
		destDao = new MappingFileDao(dest);
	}

	public void run() {
		log.trace("import mapping files");
		try {
			for (MappingFile sourceFile : sourceDao.getAll()) {
				syncFile(sourceFile);
			}
		} catch (Exception e) {
			log.error("failed to import mapping files", e);
		}
	}

	private void syncFile(MappingFile sourceFile) {
		if (sourceFile == null || sourceFile.getContent() == null)
			return;
		MappingFile destFile = destDao.getForFileName(sourceFile.getFileName());
		if (destFile != null) {
			log.trace("the mapping file {} already exist in the database and " +
					"was not changed", destFile);
			return;
		}
		log.trace("copy mapping file {}", sourceFile);
		destFile = new MappingFile();
		destFile.setContent(sourceFile.getContent());
		destFile.setFileName(sourceFile.getFileName());
		destDao.insert(destFile);
	}

}
