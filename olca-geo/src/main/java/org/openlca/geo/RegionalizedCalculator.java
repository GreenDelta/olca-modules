package org.openlca.geo;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.DataStructures;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.matrix.CostVector;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.results.FullResult;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.Scope;
import org.openlca.geo.kml.LocationKml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionalizedCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final CalculationSetup setup;
	private final IMatrixSolver solver;

	public RegionalizedCalculator(CalculationSetup setup, IMatrixSolver solver) {
		this.setup = setup;
		this.solver = solver;
	}

	public RegionalizedResult calculate(IDatabase db, MatrixCache cache) {
		return calculate(db, cache, null);
	}

	public RegionalizedResult calculate(IDatabase db, MatrixCache cache,
			RegionalizationSetup regioSetup) {
		try {
			// TODO: sub-systems are currently not supported
			MatrixData m = DataStructures.matrixData(
					setup, solver, cache, Collections.emptyMap());
			if (regioSetup == null)
				regioSetup = RegionalizationSetup.create(
						db, setup.impactMethod, m.techIndex);
			if (!regioSetup.canCalculate)
				return null;

			FormulaInterpreter interpreter = DataStructures.interpreter(
					db, setup, m.techIndex);

			ImpactTable impactTable = ImpactTable.build(cache,
					setup.impactMethod.id, m.enviIndex);

			FullResult r = new FullResult();
			r.flowIndex = m.enviIndex;
			r.techIndex = m.techIndex;
			r.impactIndex = impactTable.impactIndex;

			// direct LCI results
			LcaCalculator baseCalc = new LcaCalculator(solver, m);
			IMatrix inverse = solver.invert(m.techMatrix);
			r.scalingVector = baseCalc.getScalingVector(inverse, r.techIndex);
			r.directFlowResults = m.enviMatrix.copy();
			solver.scaleColumns(r.directFlowResults, r.scalingVector);
			r.totalRequirements = baseCalc.getTotalRequirements(
					m.techMatrix, r.scalingVector);
			r.techMatrix = m.techMatrix.copy();
			solver.scaleColumns(r.techMatrix, r.scalingVector);

			// assessed intervention matrix
			IMatrix factors = impactTable.createMatrix(solver, interpreter);
			r.impactFactors = factors;
			IMatrix assessedEnvi = solver.multiply(factors, m.enviMatrix);
			eachKml(regioSetup, impactTable, interpreter, (kml, kmlFactors) -> {
				IMatrix assessedKml = solver.multiply(kmlFactors, m.enviMatrix);
				for (ProcessProduct product : kml.processProducts) {
					int col = r.techIndex.getIndex(product);
					for (int row = 0; row < assessedEnvi.rows(); row++) {
						assessedEnvi.set(row, col, assessedKml.get(row, col));
					}
				}
			});

			// direct LCIA results
			r.directImpactResults = assessedEnvi.copy();
			solver.scaleColumns(r.directImpactResults, r.scalingVector);

			// upstream & total results
			r.loopFactor = LcaCalculator.getLoopFactor(
					m.techMatrix, r.scalingVector, r.techIndex);
			double[] demands = baseCalc.getRealDemands(
					r.totalRequirements, r.loopFactor);
			r.upstreamFlowResults = solver.multiply(m.enviMatrix, inverse);
			solver.scaleColumns(r.upstreamFlowResults, demands);
			r.upstreamImpactResults = solver.multiply(assessedEnvi, inverse);
			solver.scaleColumns(r.upstreamImpactResults, demands);
			int refIdx = r.techIndex.getIndex(r.techIndex.getRefFlow());
			r.totalFlowResults = r.upstreamFlowResults.getColumn(refIdx);
			r.totalImpactResults = r.upstreamImpactResults.getColumn(refIdx);

			// add LCC results
			if (m.costVector != null) {

				// direct LCC
				double[] costValues = m.costVector;
				double[] directCosts = new double[costValues.length];
				for (int i = 0; i < r.scalingVector.length; i++) {
					directCosts[i] = costValues[i] * r.scalingVector[i];
				}
				r.directCostResults = directCosts;

				// upstream LCC
				IMatrix costMatrix = CostVector.asMatrix(solver, costValues);
				IMatrix upstreamCosts = solver.multiply(costMatrix, inverse);
				solver.scaleColumns(upstreamCosts, demands);
				r.totalCosts = upstreamCosts.get(0, refIdx);
				r.upstreamCostResults = upstreamCosts;
			}

			return new RegionalizedResult(r, regioSetup.kmlData,
					regioSetup.parameterSet);
		} catch (Exception e) {
			log.error("failed to calculate regionalized result", e);
			return null;
		}
	}

	private void eachKml(RegionalizationSetup regioSetup, ImpactTable table,
			FormulaInterpreter interpreter, BiConsumer<LocationKml, IMatrix> fn) {
		Scope scope = interpreter.getScope(setup.impactMethod.id);
		for (LocationKml kml : regioSetup.kmlData) {
			Map<String, Double> params = regioSetup.parameterSet.get(
					kml.locationId);
			for (String param : params.keySet()) {
				Double val = params.get(param);
				if (val == null)
					continue;
				scope.bind(param, val.toString());
			}
			IMatrix factors = table.createMatrix(solver, interpreter);
			fn.accept(kml, factors);
		}
	}
}
