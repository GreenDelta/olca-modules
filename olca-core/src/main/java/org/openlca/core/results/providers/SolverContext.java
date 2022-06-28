package org.openlca.core.results.providers;

import org.openlca.core.DataDir;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.solvers.MatrixSolver;

public class SolverContext {

	private final IDatabase db;
	private final MatrixData matrixData;
	private LibraryDir libDir;
	private LibraryCache libraries;
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

	public Demand demand() {
		return matrixData.demand;
	}

	public MatrixData data() {
		return matrixData;
	}

	public boolean hasLibraryLinks() {
		return matrixData.hasLibraryLinks();
	}

	public SolverContext libraryDir(LibraryDir dir) {
		this.libDir = dir;
		return this;
	}

	public LibraryCache libraries() {
		if (libraries != null)
			return libraries;
		if (libDir == null) {
			libDir = DataDir.get().getLibraryDir();
		}
		libraries = new LibraryCache(libDir, db);
		return libraries;
	}

	public SolverContext solver(MatrixSolver solver) {
		this.solver = solver;
		return this;
	}

	public MatrixSolver solver() {
		if (solver == null) {
			solver = MatrixSolver.get();
		}
		return solver;
	}
}
