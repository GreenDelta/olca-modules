package org.openlca.core.database.upgrades;

import java.util.List;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;

class Upgrade3 implements IUpgrade {

	private IDatabase database;

	@Override
	public int[] getInitialVersions() {
		return new int[] { 4 };
	}

	@Override
	public int getEndVersion() {
		return 5;
	}

	@Override
	public void exec(IDatabase database) throws Exception {
		this.database = database;
		updateCategoryRefIds();
	}

	private void updateCategoryRefIds() {
		CategoryDao dao = new CategoryDao(database);
		List<Category> roots = dao.getRootCategories();
		for (Category category : roots)
			dao.update(category);
	}
	
}
