package org.openlca.io.refdata;

import java.util.List;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.supercsv.io.CsvListWriter;

class ImpactMethodExport extends AbstractExport {

	@Override
	protected void doIt(CsvListWriter writer, IDatabase database) throws Exception {
		log.trace("write impact methods");
		ImpactMethodDao dao = new ImpactMethodDao(database);
		CategoryDao categoryDao = new CategoryDao(database);
		List<ImpactMethodDescriptor> methods = dao.getDescriptors();
		for (ImpactMethodDescriptor method : methods) {
			Object[] line = createLine(method, categoryDao);
			writer.write(line);
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
