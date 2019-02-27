package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.solvers.IMatrixSolver;

class SimulationGraph {

	private static class Node {
		ProcessProduct product;
		MatrixData data;
		ParameterTable parameters;
	}

	private final IDatabase db;
	private final CalculationSetup setup;
	private final IMatrixSolver solver;

	private SimulationGraph(
			IDatabase db,
			CalculationSetup setup,
			IMatrixSolver solver) {
		this.db = db;
		this.setup = setup;
		this.solver = solver;
	}



}
