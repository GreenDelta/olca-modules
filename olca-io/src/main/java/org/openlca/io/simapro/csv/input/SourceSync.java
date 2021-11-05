package org.openlca.io.simapro.csv.input;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.refdata.LiteratureReferenceBlock;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SourceSync {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final IDatabase db;
	private final Map<String, Source> sources = new HashMap<>();

	SourceSync(IDatabase db) {
		this.db = db;
	}

	public Map<String, Source> sources() {
		return sources;
	}

	void sync(CsvDataSet dataSet) {
		log.trace("synchronize sources with database");
		try {
			for (var block : dataSet.literatureReferences()) {
				var source = sync(block);
				if (source == null) {
					log.warn("could not synchronize {} with DB", block);
					continue;
				}
				sources.put(block.name(), source);
			}
		} catch (Exception e) {
			log.error("failed to synchronize sources with database");
		}
	}

	private Source sync(LiteratureReferenceBlock block) {
		if (block == null)
			return null;
		var refId = KeyGen.get(block.name(), block.category());
		var source = db.get(Source.class, refId);
		return source != null
			? source
			: create(refId, block);
	}

	private Source create(String refId, LiteratureReferenceBlock block) {
		var source = new Source();
		source.refId = refId;
		source.name = block.name();
		source.category = Strings.nullOrEmpty(block.category())
			? null
			: CategoryDao.sync(db, ModelType.SOURCE, block.category());
		source.description = block.description();
		source.url = block.documentationLink();
		return db.insert(source);
	}
}
