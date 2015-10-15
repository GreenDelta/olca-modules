package org.openlca.core.database;

import java.sql.Connection;
import java.sql.ResultSet;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;

public class NativeConnectionTest {

	@Test
	public void testConnection() throws Exception {
		IDatabase database = Tests.getDb();
		Connection con = database.createConnection();
		ResultSet results = con.createStatement().executeQuery(
				"select count(*) from tbl_units");
		Assert.assertTrue(results.next());
		long count = results.getLong(1);
		Assert.assertTrue(count >= 0);
		results.close();
		con.close();
	}

}
