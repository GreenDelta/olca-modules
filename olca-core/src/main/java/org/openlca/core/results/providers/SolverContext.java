package org.openlca.core.results.providers;

import org.openlca.core.DataDir;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.matrix.solvers.JuliaSolver;
import org.openlca.nativelib.NativeLib;

public class SolverContext {

	private final IDatabase db;
	private final MatrixData matrixData;
	private LibraryDir libraryDir;
	private MatrixSolver solver;

	private SolverContext(IDatabase db, MatrixData matrixData) {
		this.db = db;
		this.matrixData = matrixData;
	}

	public static SolverContext of(IDatabase db, MatrixData matrixData) {
		return new SolverContext(db, matrixData);
	}

	/**
	 * Creates a solver context without a database. Note that a calculation with
	 * such a context can fail when data from a database are required (e.g. to
	 * resolve resources from linked libraries).
	 */
	public static SolverContext of(MatrixData matrixData) {
		return new SolverContext(null, matrixData);
	}

	public boolean hasDb() {
		return db != null;
	}

	public IDatabase db() {
		return db;
	}

	public MatrixData matrixData() {
		return matrixData;
	}

	public boolean hasLibraryLinks() {
		return matrixData.hasLibraryLinks();
	}

	public SolverContext libraryDir(LibraryDir libraryDir) {
		this.libraryDir = libraryDir;
		return this;
	}

	public LibraryDir libraryDir() {
		return libraryDir == null
			? DataDir.getLibraryDir()
			: libraryDir;
	}

	public SolverContext solver(MatrixSolver solver) {
		this.solver = solver;
		return this;
	}

	public MatrixSolver solver() {
		if (solver != null)
			return solver;
		if (NativeLib.isLoaded()) {
			solver = new JuliaSolver();
			return solver;
		}
		solver = new JavaSolver();
		return solver;
	}
}
