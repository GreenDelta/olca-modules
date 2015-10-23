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

	public RegionalizedResult calculate(RegionalizationSetup setup, FullResult baseResult,
			FormulaInterpreter interpreter, ImpactTable impactTable) {
		this.setup = setup;
		this.interpreter = interpreter;
		this.impactTable = impactTable;
		try {
			RegionalizedResult result = new RegionalizedResult();
			result.setBaseResult(baseResult);
			FullResult regioResult = calcRegioResult(baseResult);
			result.setRegionalizedResult(regioResult);
			return result;
		} catch (Exception e) {
			log.error("failed to calculate regionalized result", e);
		}
		return null;
	}

	private FullResult calcRegioResult(FullResult baseResult) {
		List<KmlLoadResult> features = setup.getKmlData();
		ParameterSet parameterSet = setup.getParameterSet();
		FullResult regioResult = initRegioResult(baseResult);
		IMatrix impactResultMatrix = regioResult.singleImpactResults;
		Map<LongPair, Integer> indices = getIndices(regioResult.productIndex);
		for (KmlLoadResult result : features) {
			Map<String, Double> parameters = parameterSet.getFor(result.getLocationId());
			ImpactMatrix impacts = createImpactMatrix(parameters);
			IMatrix factors = impacts.getFactorMatrix();
			for (LongPair processProduct : result.getProcessProducts()) {
				int index = indices.get(processProduct);
				double[] flowResults = baseResult.singleFlowResults.getColumn(index);
				double[] impactResults = solver.multiply(factors, flowResults);
				for (int row = 0; row < impactResults.length; row++)
					impactResultMatrix.setEntry(row, index, impactResults[row]);
			}
		}
		calcTotalImpactResult(regioResult);
		return regioResult;
	}

	private Map<LongPair, Integer> getIndices(ProductIndex index) {
		Map<LongPair, Integer> indices = new HashMap<>();
		for (int i = 0; i < index.size(); i++) {
			LongPair processProduct = index.getProductAt(i);
			indices.put(processProduct, i);
		}
		return indices;
	}

	private FullResult initRegioResult(FullResult baseResult) {
		FullResult regioResult = new FullResult();
		regioResult.productIndex = baseResult.productIndex;
		regioResult.flowIndex = baseResult.flowIndex;
		regioResult.impactIndex = baseResult.impactIndex;
		regioResult.impactFactors = baseResult.impactFactors;
		regioResult.totalFlowResults = baseResult.totalFlowResults;
		regioResult.scalingFactors = baseResult.scalingFactors;
		regioResult.singleFlowResults = baseResult.singleFlowResults;
		regioResult.singleFlowImpacts = baseResult.singleFlowImpacts;
		regioResult.singleImpactResults = baseResult.singleImpactResults.copy();
		regioResult.linkContributions = baseResult.linkContributions;
		regioResult.upstreamFlowResults = baseResult.upstreamFlowResults;
		regioResult.upstreamImpactResults = baseResult.upstreamImpactResults;
		return regioResult;
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
		for (int row = 0; row < singleResults.getRowDimension(); row++) {
			for (int col = 0; col < singleResults.getColumnDimension(); col++) {
				totalResults[row] += singleResults.getEntry(row, col);
			}
		}
		regioResult.totalImpactResults = totalResults;
	}

}
