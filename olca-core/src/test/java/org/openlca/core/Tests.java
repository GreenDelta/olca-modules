package org.openlca.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.matrix.solvers.NativeSolver;
import org.openlca.core.matrix.solvers.mkl.MKL;
import org.openlca.core.matrix.solvers.mkl.MKLSolver;
import org.openlca.nativelib.NativeLib;

public class Tests {

	private static final boolean USE_FILE_BASED_DB = false;
	private static IDatabase db;
	private static List<MatrixSolver> solvers;
	private static MatrixSolver defaultSolver;

	public static List<MatrixSolver> getSolvers() {
		if (solvers != null)
			return solvers;
		synchronized (Tests.class) {
			if (solvers != null)
				return solvers;
			solvers = new ArrayList<>();
			solvers.add(new JavaSolver());
			var dataDir = DataDir.get().root();

			// the OpenBLAS based solvers
			if (!NativeLib.isLoaded() && NativeLib.isLibraryDir(dataDir)) {
				NativeLib.loadFrom(dataDir);
			}
			if (NativeLib.isLoaded()) {
				solvers.add(new NativeSolver());
			}

			// the MKL based solver
			if (!MKL.isLoaded() && MKL.isLibraryDir(dataDir)) {
				MKL.loadFrom(dataDir);
			}
			if (MKL.isLoaded()) {
				solvers.add(new MKLSolver());
			}
		}
		return solvers;
	}

	public static MatrixSolver getDefaultSolver() {
		if (defaultSolver != null)
			return defaultSolver;
		MatrixSolver solver = null;
		for (var s : getSolvers()) {
			if (s instanceof MKLSolver) {
				solver = s;
				break;
			}
			if (solver == null
					|| (!solver.isNative() && s.isNative())) {
				solver = s;
			}
		}
		defaultSolver = Objects.requireNonNull(solver);
		return defaultSolver;
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

}
