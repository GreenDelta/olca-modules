package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.SourceDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;
import org.openlca.io.Categories;
import org.openlca.io.KeyGen;
import org.openlca.simapro.csv.model.refdata.LiteratureReferenceBlock;

class SourceImport {

	private SourceDao dao;
	private IDatabase database;

	public SourceImport(IDatabase database) {
		this.database = database;
		this.dao = new SourceDao(database);
	}

	public Source run(LiteratureReferenceBlock block) {
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
		return source;
	}

	private Category getCategory(LiteratureReferenceBlock block) {
		if (block.getCategory() == null)
			return null;
		return Categories.findOrAdd(database, ModelType.SOURCE,
				new String[] { block.getCategory() });
	}
}
