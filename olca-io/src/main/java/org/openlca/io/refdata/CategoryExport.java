package org.openlca.io.refdata;

import java.util.List;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.supercsv.io.CsvListWriter;

class CategoryExport extends AbstractExport {

	@Override
	protected void doIt(CsvListWriter writer, IDatabase database)
			throws Exception {
		log.trace("write categories");
		CategoryDao dao = new CategoryDao(database);
		List<Category> categories = dao.getAll();
		for (Category category : categories) {
			Object[] line = createLine(category);
			writer.write(line);
		}
		log.trace("{} categories written", categories.size());

	}

	private Object[] createLine(Category category) {
		Object[] line = new Object[5];
		line[0] = category.getRefId();
		line[1] = category.getName();
		line[2] = category.getDescription();
		if (category.getModelType() != null)
			line[3] = category.getModelType().name();
		if (category.getCategory() != null)
			line[4] = category.getCategory().getRefId();
		return line;
	}

}
