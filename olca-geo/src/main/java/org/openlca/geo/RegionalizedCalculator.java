package org.openlca.geo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IMatrixSolver;
import org.openlca.core.matrix.ImpactMatrix;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.Scope;
import org.openlca.geo.kml.KmlLoadResult;
import org.openlca.geo.parameter.ParameterSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates a regionalized LCA result.
 */
public class RegionalizedCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());

	private IMatrixSolver solver;
	private RegionalizationSetup setup;

	private FormulaInterpreter interpreter;
	private ImpactTable impactTable;

	public RegionalizedCalculator(IMatrixSolver solver) {
		this.solver = solver;
	}

	public RegionalizedResult calculate(RegionalizationSetup setup,
			FullResult baseResult, FormulaInterpreter interpreter,
			ImpactTable impactTable) {
		this.setup = setup;
		this.interpreter = interpreter;
		this.impactTable = impactTable;
		try {
			FullResult regioResult = calcRegioResult(baseResult);
			return new RegionalizedResult(baseResult, regioResult);
		} catch (Exception e) {
			log.error("failed to calculate regionalized result", e);
			return null;
		}
	}

	private FullResult initRegioResult(FullResult baseResult) {
		FullResult regioResult = new FullResult();
		regioResult.productIndex = baseResult.productIndex;
		regioResult.flowIndex = baseResult.flowIndex;
		regioResult.impactIndex = baseResult.impactIndex;
		regioResult.scalingFactors = baseResult.scalingFactors;
		regioResult.impactFactors = baseResult.impactFactors;
		regioResult.singleFlowResults = baseResult.singleFlowResults;
		regioResult.upstreamFlowResults = baseResult.upstreamFlowResults;
		regioResult.totalFlowResults = baseResult.totalFlowResults;
		regioResult.hasCostResults = baseResult.hasCostResults;
		regioResult.singleCostResults = baseResult.singleCostResults;
		regioResult.upstreamCostResults = baseResult.upstreamCostResults;
		regioResult.totalCostResult = baseResult.totalCostResult;
		regioResult.totalRequirements = baseResult.totalRequirements;
		regioResult.singleFlowImpacts = baseResult.singleFlowImpacts;
		regioResult.linkContributions = baseResult.linkContributions;
		// copy impact results - they will be modified
		regioResult.singleImpactResults = baseResult.singleImpactResults.copy();
		regioResult.upstreamImpactResults = baseResult.upstreamImpactResults
				.copy();
		// total impact results will be recalculated later
		return regioResult;
	}

	private FullResult calcRegioResult(FullResult baseResult) {
		List<KmlLoadResult> features = setup.getKmlData();
		ParameterSet parameterSet = setup.getParameterSet();
		FullResult regioResult = initRegioResult(baseResult);
		Map<LongPair, Integer> indices = getIndices(regioResult.productIndex);
		for (KmlLoadResult result : features) {
			Map<String, Double> parameters = parameterSet.getFor(result.locationId);
			ImpactMatrix impacts = createImpactMatrix(parameters);
			IMatrix factors = impacts.getFactorMatrix();
			for (LongPair product : result.processProducts) {
				int index = indices.get(product);
				updateImpacts(index, factors, regioResult, false);
				// updateImpacts(index, factors, regioResult, true);
			}
		}
		calcTotalImpactResult(regioResult);
		return regioResult;
	}

	private void updateImpacts(int index, IMatrix factors, FullResult result,
			boolean upstream) {
		IMatrix flows = result.singleFlowResults;
		IMatrix impacts = result.singleImpactResults;
		if (upstream) {
			flows = result.upstreamFlowResults;
			impacts = result.upstreamImpactResults;
		}
		double[] flowResults = flows.getColumn(index);
		double[] impactResults = solver.multiply(factors, flowResults);
		for (int row = 0; row < impactResults.length; row++)
			impacts.setEntry(row, index, impactResults[row]);
	}

	private Map<LongPair, Integer> getIndices(ProductIndex index) {
		Map<LongPair, Integer> indices = new HashMap<>();
		for (int i = 0; i < index.size(); i++) {
			LongPair processProduct = index.getProductAt(i);
			indices.put(processProduct, i);
		}
		return indices;
	}

	private ImpactMatrix createImpactMatrix(Map<String, Double> params) {
		long methodId = setup.getImpactMethod().getId();
		Scope scope = interpreter.getScope(methodId);
		for (String param : params.keySet()) {
			Double val = params.get(param);
			if (val == null)
				continue;
			scope.bind(param, val.toString());
		}
		return impactTable.createMatrix(solver.getMatrixFactory(), interpreter);
	}

	private void calcTotalImpactResult(ContributionResult regioResult) {
		IMatrix singleResults = regioResult.singleImpactResults;
		double[] totalResults = new double[singleResults.getRowDimension()];
		for (int row = 0; row < singleResults.getRowDimension(); row++)
			for (int col = 0; col < singleResults.getColumnDimension(); col++)
				totalResults[row] += singleResults.getEntry(row, col);
		regioResult.totalImpactResults = totalResults;
	}

}
