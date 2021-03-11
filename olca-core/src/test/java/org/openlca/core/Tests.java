package org.openlca.core;

import java.io.File;

import org.openlca.core.database.BaseDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.Derby;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.AbstractEntity;

public class Tests {

	private static final boolean USE_FILE_BASED_DB = false;

	private static IDatabase db;

	public static MatrixSolver getDefaultSolver() {
		return new JavaSolver();
	}

	public static void emptyCache() {
		if (db != null) {
			db.getEntityFactory().getCache().evictAll();
		}
	}

	public static IDatabase getDb() {
		if (db == null) {
			db = USE_FILE_BASED_DB
					? initFileBasedDb()
					: Derby.createInMemory();
		}
		return db;
	}

	private static IDatabase initFileBasedDb() {
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		String dbName = "olca_test_db_1.4";
		File tmpDir = new File(tmpDirPath);
		File folder = new File(tmpDir, dbName);
		return new Derby(folder);
	}

	@SuppressWarnings("unchecked")
	public static <T extends AbstractEntity> BaseDao<T> dao(T entity) {
		return (BaseDao<T>) Daos.base(getDb(), entity.getClass());
	}

}
