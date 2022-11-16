package org.openlca.io.refdata;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

class ImpactMethodExport implements Export {

	@Override
	public void doIt(CSVPrinter printer, IDatabase db) throws IOException {
		var dao = new ImpactMethodDao(db);
		var categoryDao = new CategoryDao(db);
		for (var method : dao.getDescriptors()) {
			Object[] line = createLine(method, categoryDao);
			printer.printRecord(line);
		}
	}

	private Object[] createLine(ImpactMethodDescriptor method,
		CategoryDao categoryDao) {
		Object[] line = new Object[4];
		line[0] = method.refId;
		line[1] = method.name;
		line[2] = null; // TODO
		if (method.category != null) {
			Category category = categoryDao.getForId(method.category);
			if (category != null)
				line[3] = category.refId;
		}
		return line;
	}
}
