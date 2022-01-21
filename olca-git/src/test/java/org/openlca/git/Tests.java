package org.openlca.git;

import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;

public class Tests {

	private static final IDatabase db = Derby.createInMemory();

	public static IDatabase db() {
		return db;
	}
}
