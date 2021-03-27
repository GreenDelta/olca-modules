package org.openlca.core.results.providers;

import org.openlca.core.DataDir;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.library.LibraryMatrix;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.ImpactIndex;
import org.openlca.core.matrix.IndexedMatrix;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.solvers.MatrixSolver;

// currently under development; do not use this for now
class EagerLibraryProvider implements ResultProvider {

	private final IDatabase db;
	private final MatrixData dbData;
	private final LibraryDir libDir;
	private final MatrixSolver solver;

	private final MatrixData fullData;

	private EagerLibraryProvider(IDatabase db, MatrixData dbData) {
		this.db = db;
		this.libDir = DataDir.getLibraryDir();
		this.solver = MatrixSolver.Instance.getNew();
		this.dbData = dbData;

		fullData = new MatrixData();
		fullData.impactMatrix = dbData.impactMatrix;
		var libTechIndices = LibUtil.loadTechIndicesOf(
			dbData.techIndex, libDir, db);
		fullData.techIndex = LibUtil.combinedTechIndexOf(
			dbData.techIndex, libTechIndices.values());
		var libFlowIndices = LibUtil.loadFlowIndicesOf(
			libTechIndices.keySet(), libDir, db);
		fullData.flowIndex = LibUtil.combinedFlowIndexOf(
			dbData.flowIndex, libFlowIndices.values());

		// build the combined tech-matrix
		var techBuilder = IndexedMatrix.build(fullData.techIndex)
			.put(IndexedMatrix.of(dbData.techIndex, dbData.techMatrix));
		libTechIndices
			.forEach((libID, techIdx) -> libDir.getMatrix(libID, LibraryMatrix.A)
				.ifPresent(m -> techBuilder.put(IndexedMatrix.of(techIdx, m))));
		fullData.techMatrix = techBuilder.finish().data();

		// build the combined intervention matrix
		var flowBuilder = IndexedMatrix.build(
			fullData.flowIndex, fullData.techIndex);
		if (dbData.flowMatrix != null) {
			flowBuilder.put(IndexedMatrix.of(
				dbData.flowIndex,
				dbData.techIndex,
				dbData.flowMatrix));
		}
		libFlowIndices.forEach((libID, flowIdx) ->
			libDir.getMatrix(libID, LibraryMatrix.B).ifPresent(m -> {
				var techIdx = libTechIndices.get(libID);
				if (techIdx == null)
					return;
				flowBuilder.put(IndexedMatrix.of(flowIdx, techIdx, m));
			}));
	}

	@Override
	public TechIndex techIndex() {
		return fullData.techIndex;
	}

	@Override
	public FlowIndex flowIndex() {
		return fullData.flowIndex;
	}

	@Override
	public ImpactIndex impactIndex() {
		return fullData.impactIndex;
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
