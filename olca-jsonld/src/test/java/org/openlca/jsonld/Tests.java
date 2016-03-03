package org.openlca.jsonld;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

public class Tests {

	private static IDatabase db;

	public static IDatabase getDb() {
		if (db == null)
			db = DerbyDatabase.createInMemory();
		return db;
	}

	public static void clearDb() {
		ModelType[] types = { ModelType.PROJECT, ModelType.PRODUCT_SYSTEM,
				ModelType.PROCESS, ModelType.IMPACT_METHOD, ModelType.FLOW,
				ModelType.FLOW_PROPERTY, ModelType.UNIT_GROUP,
				ModelType.LOCATION, ModelType.ACTOR, ModelType.SOURCE,
				ModelType.CURRENCY, ModelType.SOCIAL_INDICATOR,
				ModelType.PARAMETER, ModelType.CATEGORY };
		for (ModelType type : types)
			Daos.createCategorizedDao(getDb(), type).deleteAll();
		CategoryDao dao = new CategoryDao(getDb());
		for (Category root : dao.getRootCategories())
			dao.delete(root);
	}

}
