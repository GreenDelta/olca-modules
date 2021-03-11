package org.openlca.io;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.Derby;

public class Tests {

	private static IDatabase db;

	public static IDatabase getDb() {
		if (db == null)
			db = Derby.createInMemory();
		return db;
	}
}
