package org.openlca.core.results.providers;

import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.MatrixReader;

public class InversionSolver {

	private final MatrixData data;

	private MatrixReader inverse;
	private double[] scalingVector;
	private double[] totalRequirements;
	private double[] loopFactors;

	private double[] totalInventory;
	private MatrixReader directInventories;
	private MatrixReader inventoryIntensities;

	private double[] totalImpacts;
	private MatrixReader directImpacts;
	private MatrixReader impactIntensities;

	private double totalCosts;
	private double[] directCosts;
	private double[] costIntensities;

	private InversionSolver(MatrixData data) {
		this.data = data;
	}

	public static InversionSolver of(MatrixData data) {
		return new InversionSolver(data);
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

		return null;
	}


}
