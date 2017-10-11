package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.SourceDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;
import org.openlca.io.Categories;
import org.openlca.simapro.csv.model.refdata.LiteratureReferenceBlock;
import org.openlca.util.KeyGen;
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

	public void run(RefData refData) {
		log.trace("synchronize sources with database");
		try {
			for (LiteratureReferenceBlock block : index
					.getLiteratureReferences()) {
				Source source = sync(block);
				if (source == null)
					log.warn("could not synchronize {} with DB", block);
				else
					refData.putSource(block.getName(), source);
			}
		} catch (Exception e) {
			log.error("failed to synchronize sources with database");
		}
	}

	private Source sync(LiteratureReferenceBlock block) {
		if (block == null)
			return null;
		String refId = KeyGen.get(block.getName(), block.getCategory());
		Source source = dao.getForRefId(refId);
		if (source != null)
			return source;
		else
			return create(refId, block);
	}

	private Source create(String refId, LiteratureReferenceBlock block) {
		Source source = new Source();
		source.setRefId(refId);
		source.setName(block.getName());
		source.setCategory(getCategory(block));
		source.setDescription(block.getDescription());
		source.setTextReference(block.getDocumentationLink());
		new SourceDao(database).insert(source);
		return source;
	}

	private Category getCategory(LiteratureReferenceBlock block) {
		if (block.getCategory() == null)
			return null;
		return Categories.findOrAdd(database, ModelType.SOURCE,
				new String[] { block.getCategory() });
	}
}
