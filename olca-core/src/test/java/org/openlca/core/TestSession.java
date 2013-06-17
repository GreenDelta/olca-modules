package org.openlca.core;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.mysql.MySQLDatabase;

public class TestSession {

	private static IDatabase mysqlDatabase;

	public static IDatabase getMySQLDatabase() {
		if (mysqlDatabase != null)
			return mysqlDatabase;
		String url = "jdbc:mysql://localhost:3306/olca_test_db";
		String user = "root";
		String password = null;
		mysqlDatabase = new MySQLDatabase(url, user, password);
		System.out.println(mysqlDatabase.getName());
		return mysqlDatabase;
	}

	public static void emptyCache() {
		if (mysqlDatabase != null) {
			mysqlDatabase.getEntityFactory().getCache().evictAll();
		}
	}

}
