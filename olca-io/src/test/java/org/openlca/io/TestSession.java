package org.openlca.io;

import org.openlca.core.database.DatabaseContent;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;

public class TestSession {

	private static IDatabase derbyDatabase;

	public static IDatabase getDerbyDatabase() {
		if (derbyDatabase == null)
			derbyDatabase = DerbyDatabase
					.createInMemory(DatabaseContent.ALL_REF_DATA);
		return derbyDatabase;
	}
}
