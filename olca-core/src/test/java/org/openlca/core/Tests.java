package org.openlca.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.BaseDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.CategorizedEntity;

public class Tests {

	private static final boolean USE_FILE_BASED_DB = false;

	private static IDatabase db;

	public static IMatrixSolver getDefaultSolver() {
		return new JavaSolver();
	}

	public static void emptyCache() {
		if (db != null) {
			db.getEntityFactory().getCache().evictAll();
		}
	}

	public static IDatabase getDb() {
		if (db == null) {
			if (USE_FILE_BASED_DB)
				db = initFileBasedDb();
			else
				db = DerbyDatabase.createInMemory();
		}
		return db;
	}

	private static IDatabase initFileBasedDb() {
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		String dbName = "olca_test_db_1.4";
		File tmpDir = new File(tmpDirPath);
		File folder = new File(tmpDir, dbName);
		return new DerbyDatabase(folder);
	}

	public static <T extends CategorizedEntity> T insert(T e) {
		if (e == null)
			return null;
		return dao(e).insert(e);
	}

	public static <T extends CategorizedEntity> T update(T e) {
		if (e == null)
			return null;
		return dao(e).update(e);
	}

	public static <T extends CategorizedEntity> void delete(T e) {
		if (e == null)
			return;
		dao(e).delete(e);
	}

	@SuppressWarnings("unchecked")
	public static <T> BaseDao<T> dao(T entity) {
		return (BaseDao<T>) new BaseDao<>(entity.getClass(), getDb());
	}

	public static void clearDb() {
		try {
			IDatabase db = getDb();
			List<String> tables = new ArrayList<>();
			// type = T means user table
			String sql = "SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLETYPE = 'T'";
			NativeSql.on(db).query(sql, r -> {
				tables.add(r.getString(1));
				return true;
			});
			for (String table : tables) {
				if (table.equalsIgnoreCase("SEQUENCE"))
					continue;
				if (table.equalsIgnoreCase("OPENLCA_VERSION"))
					continue;
				NativeSql.on(db).runUpdate("DELETE FROM " + table);
			}
			NativeSql.on(db).runUpdate("UPDATE SEQUENCE SET SEQ_COUNT = 0");
			db.getEntityFactory().getCache().evictAll();
		} catch (Exception e) {
			throw new RuntimeException("failed to clear database", e);
		}
	}
}
