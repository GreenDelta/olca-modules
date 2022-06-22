package org.openlca.core.results.providers;

import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.solvers.MatrixSolver;

public class InversionSolver {

	private final MatrixData data;
	private final MatrixSolver solver;

	private MatrixReader inverse;
	private double[] scalingVector;
	private double[] totalRequirements;
	private double[] loopFactors;

	private double[] totalInventory;
	private Matrix directInventories;
	private MatrixReader inventoryIntensities;

	private double[] totalImpacts;
	private MatrixReader directImpacts;
	private MatrixReader impactIntensities;

	private double totalCosts;
	private double[] directCosts;
	private double[] costIntensities;

	private InversionSolver(MatrixSolver solver, MatrixData data) {
		this.solver = solver;
		this.data = data;
	}

	public static InversionSolver of(MatrixSolver solver, MatrixData data) {
		return new InversionSolver(solver, data);
	}

	public InversionSolver withInverse(MatrixReader inv) {
		this.inverse = inv;
		return this;
	}

	public InversionSolver withInventoryIntensities(MatrixReader m) {
		this.inventoryIntensities = m;
		return this;
	}

	public InversionResult calculate() {
		var techIdx = data.techIndex;
		var refIdx = techIdx.of(data.demand.techFlow());
		var inv = inverse();

		// calculate the scaling vector
		scalingVector = inv.getColumn(refIdx);
		var demandVal = data.demand.value();
		for (int i = 0; i < scalingVector.length; i++) {
			scalingVector[i] *= demandVal;
		}

		// total requirements
		totalRequirements = data.techMatrix.diag();
		for (int i = 0; i < totalRequirements.length; i++) {
			totalRequirements[i] *= scalingVector[i];
		}

		// loop factors
		loopFactors = new double[techIdx.size()];
		for (int i = 0; i < loopFactors.length; i++) {
			var aii = data.techMatrix.get(i, i);
			var ii = inverse.get(i, i);
			var f = aii * ii;
			loopFactors[i] = f == 0
				? 1.0
				: 1 / f;
		}

		if (data.enviMatrix != null) {

			// inventory results
			directInventories = data.enviMatrix.asMutableCopy();
			directInventories.scaleColumns(scalingVector);
			var intensities = inventoryIntensities();
			totalInventory = intensities.getColumn(refIdx);
			for (int i = 0; i < totalInventory.length; i++) {
				totalInventory[i] *= demandVal;
			}

			// LCIA results
			if (data.impactMatrix != null) {

				directImpacts = solver.multiply(
					data.impactMatrix, directInventories);
				impactIntensities = solver.multiply(
					data.impactMatrix, intensities);
				totalImpacts = impactIntensities.getColumn(refIdx);
				for (int i = 0; i < totalImpacts.length; i++) {
					totalImpacts[i] *= demandVal;
				}
			}
		}

		// costs
		if (data.costVector != null) {
			var n = data.costVector.length;
			directCosts = new double[n];
			var costMatrix = solver.matrix(1, n);
			for (int j = 0; j < n; j++) {
				var costs = data.costVector[j];
				directCosts[j] = costs * scalingVector[j];
				costMatrix.set(0, j, costs);
			}
			totalCostsOfOne = solver.multiply(costMatrix, inverse).getRow(0);
			totalCosts = totalCostsOfOne(refIdx) * demand;
		}

		return InversionResult.;
	}

	MatrixData data() {
		return data;
	}

	MatrixReader inverse() {
		if (inverse == null) {
			inverse = solver.invert(data.techMatrix);
		}
		return inverse;
	}

	double[] scalingVector() {
		return scalingVector;
	}

	double[] totalRequirements() {
		return totalRequirements;
	}

	double[] loopFactors() {
		return loopFactors;
	}

	double[] totalInventory() {
		return totalInventory;
	}

	MatrixReader directInventories() {
		return directInventories;
	}

	MatrixReader inventoryIntensities() {
		if (inventoryIntensities == null && data.enviMatrix != null) {
			inventoryIntensities = solver.multiply(data.enviMatrix, inverse());
		}
		return inventoryIntensities;
	}

	double[] totalImpacts() {
		return totalImpacts;
	}

	MatrixReader directImpacts() {
		return directImpacts;
	}

	MatrixReader impactIntensities() {
		return impactIntensities;
	}

	double totalCosts() {
		return totalCosts;
	}

	double[] directCosts() {
		return directCosts;
	}

	double[] costIntensities() {
		return costIntensities;
	}
}
