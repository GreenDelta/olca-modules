package org.openlca.io.ecospold2.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.SourceDao;
import org.openlca.ecospold2.EcoSpold2;
import org.openlca.ecospold2.master.Source;
import org.openlca.ecospold2.master.SourceList;
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
			for (Source source : sourceList.sources) {
				org.openlca.core.model.Source olcaSource = dao
						.getForRefId(source.id);
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
		if (source.firstAuthor != null) {
			title.append(source.firstAuthor);
			shortTitle.append(source.firstAuthor);
		}
		if (source.additionalAuthors != null) {
			title.append(", ").append(source.additionalAuthors);
			shortTitle.append(" et al.");
		}
		if (source.title != null)
			title.append(": ").append(source.title).append(".");
		if (source.placeOfPublications != null)
			title.append(" ").append(source.placeOfPublications);
		if (source.year != null) {
			title.append(" ").append(source.year);
			shortTitle.append(" ").append(source.year);
		}
		olcaSource.setName(shortTitle.toString());
		olcaSource.setDescription(source.comment);
		olcaSource.setTextReference(title.toString());
		dao.update(olcaSource);
	}
}
