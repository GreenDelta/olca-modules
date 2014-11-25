package org.openlca.geo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IMatrixSolver;
import org.openlca.core.matrix.ImpactMatrix;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.results.ContributionResult;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.Scope;
import org.openlca.geo.kml.KmlFeature;
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
			ContributionResult baseResult, FormulaInterpreter interpreter,
			ImpactTable impactTable) {
		this.setup = setup;
		this.interpreter = interpreter;
		this.impactTable = impactTable;
		try {
			RegionalizedResult result = new RegionalizedResult();
			result.setBaseResult(baseResult);
			ContributionResult regioResult = calcRegioResult(baseResult);
			result.setRegionalizedResult(regioResult);
			result.setKmlFeatures(setup.getFeatures());
			return result;
		} catch (Exception e) {
			log.error("failed to calculate regionalized result", e);
		}
		return null;
	}

	/*
	 * For optimized memory usage this method is a bit more complicated than it
	 * would be necessary without memory issues.
	 * 
	 * Instead of just iterating over the product index entries, the entries
	 * first get sorted by features. This way the regio result for features that
	 * are used multiple times does only need to be calculated once without the
	 * need to cache each result. Only the previous result needs to be cached
	 */
	private ContributionResult calcRegioResult(ContributionResult baseResult) {
		Map<LongPair, KmlFeature> features = setup.getFeatures();
		ParameterSet parameterSet = setup.getParameterSet();
		ContributionResult regioResult = initRegioResult(baseResult);
		IMatrix impactResultMatrix = regioResult.getSingleImpactResults();
		List<IndexedLongPair> sortedIndex = sortByFeatures(baseResult
				.getProductIndex());
		KmlFeature previousFeature = null;
		ImpactMatrix previousRegioImpacts = null;
		for (IndexedLongPair processProduct : sortedIndex) {
			if (!features.containsKey(processProduct))
				continue;
			KmlFeature feature = features.get(processProduct);
			ImpactMatrix regioImpacts = null;
			if (feature.equals(previousFeature))
				regioImpacts = previousRegioImpacts;
			if (regioImpacts == null)
				regioImpacts = createRegioImpacts(parameterSet.getFor(feature));
			double[] flowResults = baseResult.getSingleFlowResults().getColumn(
					processProduct.getIndex());
			double[] impactResults = solver.multiply(
					regioImpacts.getFactorMatrix(), flowResults);
			for (int row = 0; row < impactResults.length; row++) {
				impactResultMatrix.setEntry(row, processProduct.getIndex(),
						impactResults[row]);
			}
			previousFeature = feature;
			previousRegioImpacts = regioImpacts;
		}
		calcTotalImpactResult(regioResult);
		return regioResult;
	}

	private List<IndexedLongPair> sortByFeatures(ProductIndex index) {
		List<IndexedLongPair> collected = new ArrayList<IndexedLongPair>();
		for (int i = 0; i < index.size(); i++) {
			LongPair processProduct = index.getProductAt(i);
			collected.add(new IndexedLongPair(processProduct, i));
		}
		Collections.sort(collected,
				new ByFeatureComparator(setup.getFeatures()));
		return collected;
	}

	private ContributionResult initRegioResult(ContributionResult baseResult) {
		ContributionResult regioResult = new ContributionResult();
		regioResult.setProductIndex(baseResult.getProductIndex());
		regioResult.setFlowIndex(baseResult.getFlowIndex());
		regioResult.setImpactIndex(baseResult.getImpactIndex());
		regioResult.setTotalFlowResults(baseResult.getTotalFlowResults());
		regioResult.setScalingFactors(baseResult.getScalingFactors());
		regioResult.setSingleFlowResults(baseResult.getSingleFlowResults());
		regioResult.setSingleImpactResults(baseResult.getSingleImpactResults()
				.copy());
		regioResult.setLinkContributions(baseResult.getLinkContributions());
		return regioResult;
	}

	private ImpactMatrix createRegioImpacts(Map<String, Double> params) {
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
		IMatrix singleResults = regioResult.getSingleImpactResults();
		double[] totalResults = new double[singleResults.getRowDimension()];
		for (int row = 0; row < singleResults.getRowDimension(); row++) {
			for (int col = 0; col < singleResults.getColumnDimension(); col++) {
				totalResults[row] += singleResults.getEntry(row, col);
			}
		}
		regioResult.setTotalImpactResults(totalResults);
	}

	private class IndexedLongPair extends LongPair {

		private int index;

		public IndexedLongPair(LongPair longPair, int index) {
			super(longPair.getFirst(), longPair.getSecond());
			this.index = index;
		}

		public int getIndex() {
			return index;
		}

	}

	private class ByFeatureComparator implements Comparator<IndexedLongPair> {

		private Map<LongPair, KmlFeature> features;

		public ByFeatureComparator(Map<LongPair, KmlFeature> features) {
			this.features = features;
		}

		@Override
		public int compare(IndexedLongPair o1, IndexedLongPair o2) {
			KmlFeature f1 = features.get(o1);
			KmlFeature f2 = features.get(o2);
			String uuid1 = f1 != null ? f1.getIdentifier() : "";
			String uuid2 = f2 != null ? f2.getIdentifier() : "";
			return uuid1.compareTo(uuid2);
		}

	}

}
