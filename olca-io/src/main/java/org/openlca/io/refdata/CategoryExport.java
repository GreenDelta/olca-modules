package org.openlca.io.refdata;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;

class CategoryExport implements Export {

	@Override
	public void doIt(CSVPrinter writer, IDatabase db) throws IOException {
		var dao = new CategoryDao(db);
		for (var category :  dao.getAll()) {
			var line = createLine(category);
			writer.printRecord(line);
		}
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
