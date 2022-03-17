package org.openlca.io.refdata;

import java.util.List;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;

class CategoryExport extends AbstractExport {

	@Override
	protected void doIt(CSVPrinter writer, IDatabase database)
			throws Exception {
		log.trace("write categories");
		CategoryDao dao = new CategoryDao(database);
		List<Category> categories = dao.getAll();
		for (Category category : categories) {
			Object[] line = createLine(category);
			writer.printRecord(line);
		}
		log.trace("{} categories written", categories.size());

	}

	private Object[] createLine(Category category) {
		Object[] line = new Object[5];
		line[0] = category.refId;
		line[1] = category.name;
		line[2] = category.description;
		if (category.modelType != null)
			line[3] = category.modelType.name();
		if (category.category != null)
			line[4] = category.category.refId;
		return line;
	}

}
