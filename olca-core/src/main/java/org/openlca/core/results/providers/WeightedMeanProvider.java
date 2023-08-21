package org.openlca.core.results.providers;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.format.ColumnIterator;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.solvers.MatrixSolver;

/**
 * This class is used to calculate results for the raw-value version of PSILCA
 * (and should be probably never used for other things). It is a utility class
 * that wraps a result provider and returns weighted means as inventory results
 * instead of scaled sums. This is a prototype implementation of this approach
 * that is maybe used later as a calculation option for aggregating social
 * indicator values in a product system. Currently, it is using the (scaled)
 * technosphere values as weights. This means, that all of these values need to
 * be presented in the same quantity type (e.g. a monetary unit). But it could
 * also use other weights like costs which could be different for different
 * indicators.
 */
public class WeightedMeanProvider implements ResultProvider {

	private final ResultProvider r;
	private final TIntObjectHashMap<double[]> intensities;
	private final MatrixSolver solver;
	private final MatrixReader matrixB;

	private WeightedMeanProvider(ResultProvider r, MatrixSolver solver) {
		this.r = r;
		this.solver = solver;
		intensities = new TIntObjectHashMap<>();
		if (r instanceof FactorizationSolver fs) {
			matrixB = fs.matrixData().enviMatrix;
		} else if (r instanceof InversionResultProvider is) {
			matrixB = is.matrixData().enviMatrix;
		} else {
			int m = r.enviIndex().size();
			int n = r.techIndex().size();
			var dense = new DenseMatrix(m, n);
			for (var j = 0; j < n; j++) {
				for (var i = 0; i < m; i++) {
					dense.set(i, j, r.unscaledFlowOf(i, j));
				}
			}
			matrixB = dense;
		}
	}

	public static WeightedMeanProvider of(ResultProvider r) {
		return WeightedMeanProvider.of(r, null);
	}

	public static WeightedMeanProvider of(ResultProvider r, MatrixSolver solver) {
		if (!r.hasFlows())
			throw new IllegalArgumentException(
					"a result provider with inventory result must be provided");
		var s = solver == null ? MatrixSolver.get() : solver;
		return new WeightedMeanProvider(r, s);
	}

	// region: overrides

	@Override
	public double[] directFlowsOf(int techFlow) {
		return r.unscaledFlowsOf(techFlow);
	}

	@Override
	public double directFlowOf(int enviFlow, int techFlow) {
		return r.unscaledFlowOf(enviFlow, techFlow);
	}

	@Override
	public double[] totalFlowsOfOne(int techFlow) {
		var cached = intensities.get(techFlow);
		if (cached != null)
			return cached;
		var s = r.solutionOfOne(techFlow);
		var t = new double[s.length];
		double total = 0;
		for (int j = 0; j < s.length; j++) {
			var tj = Math.abs(s[j] * techValueOf(j, j));
			total += tj;
			t[j] = tj;
		}

		var g = solver.multiply(matrixB, t);
		for (var i = 0; i < g.length; i++) {
			g[i] /= total;
		}
		intensities.put(techFlow, g);
		return g;
	}

	@Override
	public double totalFlowOfOne(int enviFlow, int techFlow) {
		return totalFlowsOfOne(techFlow)[enviFlow];
	}

	@Override
	public double[] totalFlowsOf(int techFlow) {
		return totalFlowsOfOne(techFlow);
	}

	@Override
	public double totalFlowOf(int enviFlow, int techFlow) {
		return totalFlowsOfOne(techFlow)[enviFlow];
	}

	@Override
	public double[] totalFlows() {
		return totalFlowsOf(r.indexOf(r.demand().techFlow()));
	}

	// endregion

	// region: forwards

	@Override
	public Demand demand() {
		return r.demand();
	}

	@Override
	public TechIndex techIndex() {
		return r.techIndex();
	}

	@Override
	public EnviIndex enviIndex() {
		return r.enviIndex();
	}

	@Override
	public boolean hasCosts() {
		return r.hasCosts();
	}

	@Override
	public double[] scalingVector() {
		return r.scalingVector();
	}

	@Override
	public double scalingFactorOf(int techFlow) {
		return r.scalingFactorOf(techFlow);
	}

	@Override
	public double[] totalRequirements() {
		return r.totalRequirements();
	}

	@Override
	public double totalRequirementsOf(int techFlow) {
		return r.totalRequirementsOf(techFlow);
	}

	@Override
	public double[] techColumnOf(int techFlow) {
		return r.techColumnOf(techFlow);
	}

	@Override
	public ColumnIterator iterateTechColumnOf(int techFlow) {
		return r.iterateTechColumnOf(techFlow);
	}

	@Override
	public double techValueOf(int i, int j) {
		return r.techValueOf(i, j);
	}

	@Override
	public double scaledTechValueOf(int i, int j) {
		return r.scaledTechValueOf(i, j);
	}

	@Override
	public double[] solutionOfOne(int techFlow) {
		return r.solutionOfOne(techFlow);
	}

	@Override
	public double loopFactorOf(int techFlow) {
		return r.loopFactorOf(techFlow);
	}

	@Override
	public double totalFactorOf(int techFlow) {
		return r.totalFactorOf(techFlow);
	}

	@Override
	public double[] unscaledFlowsOf(int techFlow) {
		return r.unscaledFlowsOf(techFlow);
	}

	@Override
	public double unscaledFlowOf(int flow, int product) {
		return r.unscaledFlowOf(flow, product);
	}

	@Override
	public double directCostsOf(int techFlow) {
		return r.directCostsOf(techFlow);
	}

	@Override
	public double totalCostsOfOne(int techFlow) {
		return r.totalCostsOfOne(techFlow);
	}

	@Override
	public double totalCostsOf(int techFlow) {
		return r.totalCostsOf(techFlow);
	}

	@Override
	public double totalCosts() {
		return r.totalCosts();
	}

	@Override
	public void dispose() {
		r.dispose();
	}

	// endregion

	// region: skipped LCIA

	/**
	 * Always returns {@code null} as there is currently no support for LCIA
	 * results provided.
	 */
	@Override
	public ImpactIndex impactIndex() {
		return null;
	}

	@Override
	public boolean hasImpacts() {
		return false;
	}

	@Override
	public double[] impactFactorsOf(int enviFlow) {
		return ResultProvider.EMPTY_VECTOR;
	}

	@Override
	public double impactFactorOf(int indicator, int enviFlow) {
		return 0;
	}

	@Override
	public double[] flowImpactsOf(int enviFlow) {
		return ResultProvider.EMPTY_VECTOR;
	}

	@Override
	public double flowImpactOf(int indicator, int enviFlow) {
		return 0;
	}

	@Override
	public double[] directImpactsOf(int techFlow) {
		return ResultProvider.EMPTY_VECTOR;
	}

	@Override
	public double directImpactOf(int indicator, int techFlow) {
		return 0;
	}

	@Override
	public double[] totalImpactsOfOne(int techFlow) {
		return ResultProvider.EMPTY_VECTOR;
	}

	@Override
	public double totalImpactOfOne(int indicator, int techFlow) {
		return 0;
	}

	@Override
	public double[] totalImpactsOf(int techFlow) {
		return ResultProvider.EMPTY_VECTOR;
	}

	@Override
	public double totalImpactOf(int indicator, int techFlow) {
		return 0;
	}

	@Override
	public double[] totalImpacts() {
		return ResultProvider.EMPTY_VECTOR;
	}
	// endregion

}
