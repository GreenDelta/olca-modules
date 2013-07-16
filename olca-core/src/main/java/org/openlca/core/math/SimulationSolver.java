package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.lean.ImpactMethodDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A calculator that supports Monte-Carlo-Simulations.
 */
public class SimulationSolver {

	private Logger log = LoggerFactory.getLogger(getClass());
	private SimulationResult result;
	private ProductSystem system;
	private SimulationMatrix matrix;
	private ImpactMatrix impactMatrix;
	private IDatabase database;
	private AllocationMatrix allocationMatrix;

	public SimulationSolver(ProductSystem system, IDatabase database) {
		this.system = system;
		this.database = database;
	}

	public SimulationResult getResult() {
		return result;
	}

	/** TODO: allocation method = null means 'as defined in processes' */
	public void setUp(AllocationMethod allocationMethod) throws Exception {
		matrix = SimulationMatrix.create(system);
		result = new SimulationResult(matrix.getInventoryMatrix()
				.getFlowIndex());
		if (allocationMethod == AllocationMethod.None)
			return;
		if (allocationMethod == null)
			allocationMatrix = AllocationMatrix.create(
					matrix.getInventoryMatrix(), system, database);
		else
			allocationMatrix = AllocationMatrix.create(
					matrix.getInventoryMatrix(), system, allocationMethod,
					database);
	}

	/** TODO: allocation method = null means 'as defined in processes' */
	public void setUp(AllocationMethod allocationMethod,
			ImpactMethodDescriptor methodDescriptor) throws Exception {
		setUp(allocationMethod);
		ImpactMatrixBuilder builder = new ImpactMatrixBuilder(database);
		impactMatrix = builder.build(methodDescriptor, matrix
				.getInventoryMatrix().getFlowIndex());
		if (impactMatrix != null)
			result.setCategoryIndex(impactMatrix.getCategoryIndex());
	}

	public boolean canRun() {
		return result != null && matrix != null;
	}

	/**
	 * Generates random numbers and calculates the product system. Returns true
	 * if the calculation was successfully done, otherwise false (this is the
	 * case when the resulting matrix is singular).
	 */
	public boolean nextRun() {
		if (matrix == null || result == null)
			throw new IllegalStateException("setUp() not called or finished");
		try {
			InventoryMatrix inventory = matrix.nextRun();
			if (allocationMatrix != null)
				allocationMatrix.apply(inventory);
			double[] resultVector = MatrixMethod.solve(system, inventory);
			switchSigns(resultVector, inventory.getFlowIndex());
			result.appendFlowResults(resultVector);
			if (impactMatrix != null)
				calculateImpactResults(resultVector);
			return true;
		} catch (Throwable e) {
			log.trace("simulation run failed", e);
			return false;
		}
	}

	private void switchSigns(double[] resultVector, FlowIndex index) {
		for (int i = 0; i < resultVector.length; i++) {
			Flow flow = index.getFlowAt(i);
			boolean input = index.isInput(flow);
			if (input) {
				resultVector[i] = -resultVector[i];
			}
		}
	}

	private void calculateImpactResults(double[] resultVector) {
		IMatrix g = MatrixFactory.create(resultVector.length, 1);
		for (int row = 0; row < resultVector.length; row++)
			g.setEntry(row, 0, resultVector[row]);
		IMatrix resultMatrix = impactMatrix.getValues().multiply(g);
		double[] results = resultMatrix.getColumn(0);
		result.appendImpactResults(results);
	}

}
