package org.openlca.core.results.providers;

import org.openlca.core.DataDir;
import org.openlca.core.database.DataPackages;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.reader.LibReaderRegistry;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.solvers.MatrixSolver;

public class SolverContext {

	private final IDatabase db;
	private final MatrixData matrixData;
	private MatrixSolver solver;
	private LibReaderRegistry libraries;

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
		return matrixData.hasLibraryLinks(db != null ? db.getDataPackages() : new DataPackages());
	}

	public SolverContext withLibraries(LibReaderRegistry libraries) {
		this.libraries = libraries;
		return this;
	}

	public LibReaderRegistry libraries() {
		if (libraries != null)
			return libraries;
		// when no library registry is configured we try to create
		// it from the default location
		var libDir = DataDir.get().getLibraryDir();
		libraries = LibReaderRegistry.of(db, libDir);
		return libraries;
	}

	public SolverContext withSolver(MatrixSolver solver) {
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
