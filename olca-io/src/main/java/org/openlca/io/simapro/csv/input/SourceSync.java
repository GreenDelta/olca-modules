package org.openlca.io.simapro.csv.input;

import java.util.Map;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.SourceDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SourceSync {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final SpRefDataIndex index;
	private final SourceDao dao;
	private final IDatabase database;

	public SourceSync(SpRefDataIndex index, IDatabase database) {
		this.index = index;
		this.database = database;
		this.dao = new SourceDao(database);
	}

	public Map<String, Source> run(RefData refData) {
		log.trace("synchronize sources with database");
		try {
			for (LiteratureReferenceBlock block : index
					.getLiteratureReferences()) {
				Source source = sync(block);
				if (source == null)
					log.warn("could not synchronize {} with DB", block);
				else
					refData.putSource(block.name, source);
			}
		} catch (Exception e) {
			log.error("failed to synchronize sources with database");
		}
	}

	private Source sync(LiteratureReferenceBlock block) {
		if (block == null)
			return null;
		String refId = KeyGen.get(block.name, block.category);
		Source source = dao.getForRefId(refId);
		if (source != null)
			return source;
		else
			return create(refId, block);
	}

	private Source create(String refId, LiteratureReferenceBlock block) {
		Source source = new Source();
		source.refId = refId;
		source.name = block.name;
		source.category = Strings.nullOrEmpty(block.category)
			? null
			: CategoryDao.sync(database, ModelType.SOURCE, block.category);
		source.description = block.description;
		source.textReference = block.documentationLink;
		new SourceDao(database).insert(source);
		return source;
	}
}
