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
		line[0] = method.getRefId();
		line[1] = method.getName();
		line[2] = method.getDescription();
		if (method.getCategory() != null) {
			Category category = categoryDao.getForId(method.getCategory());
			if (category != null)
				line[3] = category.getRefId();
		}
		return line;
	}
}
