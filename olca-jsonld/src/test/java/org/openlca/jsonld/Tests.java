package org.openlca.jsonld;

import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.model.ModelType;

public class Tests {

	private static IDatabase db;

	public static IDatabase getDb() {
		if (db == null)
			db = DerbyDatabase.createInMemory();
		return db;
	}

	public static void clearDb() {
		for (ModelType type : ModelType.values())
			if (type.isCategorized())
				Daos.createCategorizedDao(getDb(), type).deleteAll();
	}

}
