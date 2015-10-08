package org.openlca.io;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;

public class Tests {

	private static IDatabase db;

	public static IDatabase getDb() {
		if (db == null)
			db = DerbyDatabase.createInMemory();
		return db;
	}
}
