package org.openlca.core;

import org.eclipse.persistence.jpa.PersistenceProvider;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.mysql.ConnectionData;
import org.openlca.core.database.mysql.Database;

public class TestSession {

	private static IDatabase database;

	public static IDatabase getDatabase() {
		if (database != null)
			return database;
		ConnectionData data = new ConnectionData();
		data.setDatabase("olca_test_db");
		data.setUser("root");
		data.setPersistenceProvider(new PersistenceProvider());
		database = new Database(data);
		return database;
	}

}
