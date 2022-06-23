package org.openlca.core.results.providers;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.IndexedMatrix;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;

// currently under development; do not use this for now
class EagerLibraryProvider implements ResultProvider {

	private final Demand demand;
	private final IDatabase db;
	private final MatrixData dbData;
	private final LibCache libs;
	private final MatrixData fullData;

	private EagerLibraryProvider(SolverContext context) {
		this.db = context.db();
		this.libs = LibCache.of(context);
		this.demand = context.demand();
		this.dbData = context.data();

		fullData = new MatrixData();
		fullData.impactMatrix = dbData.impactMatrix;
		var libTechIndices = libs.techIndicesOf(dbData.techIndex);
		// TODO: align index blocks
		// fullData.techIndex = LibUtil.combinedTechIndexOf(
		// 	dbData.techIndex, libTechIndices.values());
		var libFlowIndices = LibUtil.loadFlowIndicesOf(
			libTechIndices.keySet(), context.libraryDir(), db);
		fullData.enviIndex = LibUtil.combinedFlowIndexOf(
			dbData.enviIndex, libFlowIndices.values());

		// build the combined tech-matrix
		var techBuilder = IndexedMatrix.build(fullData.techIndex)
			.put(IndexedMatrix.of(dbData.techIndex, dbData.techMatrix));
		libTechIndices.forEach((libID, techIdx) -> {
			var m = libs.matrixOf(libID, LibMatrix.A);
			if (m != null) {
				techBuilder.put(IndexedMatrix.of(techIdx, m));
			}
		});
		fullData.techMatrix = techBuilder.finish().data();

		// build the combined intervention matrix
		var flowBuilder = IndexedMatrix.build(
			fullData.enviIndex, fullData.techIndex);
		if (dbData.enviMatrix != null) {
			flowBuilder.put(IndexedMatrix.of(
				dbData.enviIndex,
				dbData.techIndex,
				dbData.enviMatrix));
		}
		libFlowIndices.forEach((libID, flowIdx) -> {
			var m = libs.matrixOf(libID, LibMatrix.B);
			if (m != null) {
				var techIdx = libTechIndices.get(libID);
				if (techIdx == null)
					return;
				flowBuilder.put(IndexedMatrix.of(flowIdx, techIdx, m));
			}
		});

		// add possible impact data
		if (dbData.impactIndex != null) {
			fullData.impactIndex = dbData.impactIndex;
			fullData.impactMatrix = LibImpactMatrix.of(
					dbData.impactIndex, fullData.enviIndex)
				.withLibraryEnviIndices(libFlowIndices)
				.build(db, context.libraryDir());
		}
	}

	@Override
	public Demand demand() {
		return demand;
	}

	@Override
	public TechIndex techIndex() {
		return fullData.techIndex;
	}

	@Override
	public EnviIndex enviIndex() {
		return fullData.enviIndex;
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
	public double[] techColumnOf(int techFlow) {
		return new double[0];
	}

	@Override
	public double[] solutionOfOne(int techFlow) {
		return new double[0];
	}

	@Override
	public double loopFactorOf(int techFlow) {
		return 0;
	}

	@Override
	public double[] unscaledFlowsOf(int techFlow) {
		return new double[0];
	}

	@Override
	public double[] totalFlowsOfOne(int techFlow) {
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
	public double[] directImpactsOf(int techFlow) {
		return new double[0];
	}

	@Override
	public double[] totalImpactsOfOne(int techFlow) {
		return new double[0];
	}

	@Override
	public double[] totalImpacts() {
		return new double[0];
	}

	@Override
	public double directCostsOf(int techFlow) {
		return 0;
	}

	@Override
	public double totalCostsOfOne(int techFlow) {
		return 0;
	}

	@Override
	public double totalCosts() {
		return 0;
	}
}
