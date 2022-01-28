package org.openlca.io.refdata;

import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

class ImpactMethodExport extends AbstractExport {

	@Override
	protected void doIt(CSVPrinter printer, IDatabase db) throws IOException {
		log.trace("write impact methods");
		ImpactMethodDao dao = new ImpactMethodDao(db);
		CategoryDao categoryDao = new CategoryDao(db);
		List<ImpactMethodDescriptor> methods = dao.getDescriptors();
		for (ImpactMethodDescriptor method : methods) {
			Object[] line = createLine(method, categoryDao);
			printer.printRecord(line);
		}
		log.trace("{} impact methods written", methods.size());
	}

	private Object[] createLine(ImpactMethodDescriptor method,
		CategoryDao categoryDao) {
		Object[] line = new Object[4];
		line[0] = method.refId;
		line[1] = method.name;
		line[2] = method.description;
		if (method.category != null) {
			Category category = categoryDao.getForId(method.category);
			if (category != null)
				line[3] = category.refId;
		}
		return line;
	}
}
