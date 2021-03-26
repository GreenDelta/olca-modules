package org.openlca.core.results.providers;

import org.openlca.core.DataDir;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.ImpactIndex;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.solvers.MatrixSolver;

// currently under development; do not use this for now
class EagerLibraryProvider implements ResultProvider {

	private final IDatabase db;
	private final MatrixData foregroundData;
	private final LibraryDir libDir;
	private final MatrixSolver solver;

	private final MatrixData fullData;

	private EagerLibraryProvider(IDatabase db, MatrixData foregroundData) {
		this.db = db;
		this.libDir = DataDir.getLibraryDir();
		this.solver = MatrixSolver.Instance.getNew();
		this.foregroundData = foregroundData;

		fullData = new MatrixData();
		fullData.impactMatrix = foregroundData.impactMatrix;
		var libTechIndices = LibUtil.loadTechIndicesOf(
			foregroundData.techIndex, libDir, db);
		fullData.techIndex = LibUtil.combinedTechIndexOf(
			foregroundData.techIndex, libTechIndices.values());
		var libFlowIndices = LibUtil.loadFlowIndicesOf(
			libTechIndices.keySet(), libDir, db);
		fullData.flowIndex = LibUtil.combinedFlowIndexOf(
			foregroundData.flowIndex, libFlowIndices.values());
	}

	@Override
	public TechIndex techIndex() {
		return null;
	}

	@Override
	public FlowIndex flowIndex() {
		return null;
	}

	@Override
	public ImpactIndex impactIndex() {
		return null;
	}

	@Override
	public boolean hasCosts() {
		return false;
	}

	@Override
	public double[] scalingVector() {
		return new double[0];
	}

	@Override
	public double[] techColumnOf(int product) {
		return new double[0];
	}

	@Override
	public double[] solutionOfOne(int product) {
		return new double[0];
	}

	@Override
	public double loopFactorOf(int product) {
		return 0;
	}

	@Override
	public double[] unscaledFlowsOf(int product) {
		return new double[0];
	}

	@Override
	public double[] totalFlowsOfOne(int product) {
		return new double[0];
	}

	@Override
	public double[] totalFlows() {
		return new double[0];
	}

	@Override
	public double[] impactFactorsOf(int flow) {
		return new double[0];
	}

	@Override
	public double[] directImpactsOf(int product) {
		return new double[0];
	}

	@Override
	public double[] totalImpactsOfOne(int product) {
		return new double[0];
	}

	@Override
	public double[] totalImpacts() {
		return new double[0];
	}

	@Override
	public double directCostsOf(int product) {
		return 0;
	}

	@Override
	public double totalCostsOfOne(int product) {
		return 0;
	}

	@Override
	public double totalCosts() {
		return 0;
	}
}
