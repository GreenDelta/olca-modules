package org.openlca.io.ecospold2.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.SourceDao;
import org.openlca.ecospold2.EcoSpold2;
import org.openlca.ecospold2.Source;
import org.openlca.ecospold2.SourceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Updates *existing* source data sets that are created during a process
 * import with the source information from a EcoSpold 02 master data file.
 */
public class SourceUpdate implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private SourceDao dao;
	private File sourceFile;

	public SourceUpdate(IDatabase database, File sourceFile) {
		this.dao = new SourceDao(database);
		this.sourceFile = sourceFile;
	}

	@Override
	public void run() {
		log.trace("update sources from {}", sourceFile);
		try {
			SourceList sourceList = EcoSpold2.readSources(sourceFile);
			if (sourceList == null)
				return;
			for (Source source : sourceList.getSources()) {
				org.openlca.core.model.Source olcaSource = dao
						.getForRefId(source.getId());
				if (olcaSource == null)
					continue;
				updateSource(olcaSource, source);
			}
		} catch (Exception e) {
			log.error("failed to import sources from " + sourceFile, e);
		}
	}

	private void updateSource(org.openlca.core.model.Source olcaSource,
	                          Source source) {
		StringBuilder title = new StringBuilder();
		StringBuilder shortTitle = new StringBuilder();
		if (source.getFirstAuthor() != null) {
			title.append(source.getFirstAuthor());
			shortTitle.append(source.getFirstAuthor());
		}
		if (source.getAdditionalAuthors() != null) {
			title.append(", ").append(source.getAdditionalAuthors());
			shortTitle.append(" et al.");
		}
		if (source.getTitle() != null)
			title.append(": ").append(source.getTitle()).append(".");
		if (source.getPlaceOfPublications() != null)
			title.append(" ").append(source.getPlaceOfPublications());
		if (source.getYear() != null) {
			title.append(" ").append(source.getYear());
			shortTitle.append(" ").append(source.getYear());
		}
		olcaSource.setName(shortTitle.toString());
		olcaSource.setDescription(source.getComment());
		olcaSource.setTextReference(title.toString());
		dao.update(olcaSource);
	}
}
