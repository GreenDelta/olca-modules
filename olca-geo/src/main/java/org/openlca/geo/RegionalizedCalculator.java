package org.openlca.geo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.DataStructures;
import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IMatrixSolver;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.matrix.ImpactMatrix;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.Parameter;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.ContributionResultProvider;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates a regionalized LCA result.
 */
public class RegionalizedCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());

	private MatrixCache matrixCache;
	private IDatabase database;
	private IMatrixSolver solver;
	private IKmlLoader kmlLoader;
	private CalculationSetup setup;

	private Inventory inventory;
	private FormulaInterpreter interpreter;
	private ImpactTable impactTable;

	private Map<LongPair, KmlFeature> features;
	private List<Parameter> shapeFileParameters;
	private ShapeFileRepository repository;

	public RegionalizedCalculator(MatrixCache matrixCache, IMatrixSolver solver) {
		this(matrixCache, solver, new KmlLoader(matrixCache.getDatabase()));
	}

	public RegionalizedCalculator(MatrixCache matrixCache,
			IMatrixSolver solver, IKmlLoader kmlLoader) {
		this.matrixCache = matrixCache;
		this.database = matrixCache.getDatabase();
		this.solver = solver;
		this.kmlLoader = kmlLoader;
	}

	public RegionalizedResult calculate(CalculationSetup setup,
			EntityCache entityCache) {
		this.setup = setup;
		try {
			setUp();
			boolean canCalcRegionalized = setUpGeo();
			if (!canCalcRegionalized)
				return null;
			RegionalizedResult result = new RegionalizedResult();
			ContributionResult baseResult = calcBaseResult();
			result.setBaseResult(new ContributionResultProvider<>(baseResult,
					entityCache));
			ContributionResult regioResult = calcRegioResult(baseResult);
			result.setRegionalizedResult(new ContributionResultProvider<>(
					regioResult, entityCache));
			result.setKmlFeatures(features);
			return result;
		} catch (Exception e) {
			log.error("failed to calculate regionalized result", e);
		}
		return null;
	}

	/**
	 * Initializes the resources for plain LCA calculation.
	 */
	private void setUp() throws Exception {
		inventory = DataStructures.createInventory(setup, matrixCache);
		ParameterTable parameterTable = DataStructures.createParameterTable(
				matrixCache.getDatabase(), setup, inventory);
		interpreter = parameterTable.createInterpreter();
		if (setup.getImpactMethod() != null)
			impactTable = ImpactTable.build(matrixCache, setup
					.getImpactMethod().getId(), inventory.getFlowIndex());
	}

	private ContributionResult calcBaseResult() {
		LcaCalculator calculator = new LcaCalculator(solver);
		InventoryMatrix inventoryMatrix = inventory.createMatrix(
				solver.getMatrixFactory(), interpreter);
		if (impactTable == null)
			return calculator.calculateContributions(inventoryMatrix);
		else {
			ImpactMatrix impactMatrix = impactTable.createMatrix(
					solver.getMatrixFactory(), interpreter);
			return calculator.calculateContributions(inventoryMatrix,
					impactMatrix);
		}
	}

	/**
	 * Initializes the resources for regionalized LCIA calculation. Returns
	 * false if a regionalized calculation cannot be done and logs the
	 * respective problem in this case.
	 */
	private boolean setUpGeo() {
		shapeFileParameters = getShapeFileParameters();
		if (shapeFileParameters.isEmpty()) {
			log.warn("Cannot calculate regionalized LCIA because there is "
					+ "no LCIA method with shapefile parameters selected.");
			return false;
		}
		features = kmlLoader.load(inventory.getProductIndex());
		if (features.isEmpty()) {
			log.warn("Cannot calculate regionalized LCIA because none of the "
					+ "processes in the product system contains a KML feature.");
			return false;
		}
		return initShapeFileRepository() && shapeFilesExist();
	}

	private boolean initShapeFileRepository() {
		File shapeFileDir = new File(database.getFileStorageLocation(),
				"shapefiles");
		File methodDir = new File(shapeFileDir, setup.getImpactMethod()
				.getRefId());
		if (!methodDir.exists()) {
			log.warn("Cannot calculate regionalized LCIA because no shapefiles "
					+ "where found (location for shapefiles is "
					+ "<database file location>/shapefiles/<method uuid>");
			return false;
		}
		repository = new ShapeFileRepository(methodDir);
		return true;
	}

	private boolean shapeFilesExist() {
		if (repository == null)
			return false;
		List<String> shapeFiles = repository.getShapeFiles();
		for (Parameter parameter : shapeFileParameters) {
			String shapefile = parameter.getExternalSource();
			if (!shapeFiles.contains(shapefile)) {
				log.error(
						"Cannot calculate regionalized LCIA because shapefile "
								+ "{} referenced from parameter {} does not exist",
						shapefile, parameter);
				return false;
			}
		}
		return true;
	}

	private List<Parameter> getShapeFileParameters() {
		if (setup.getImpactMethod() == null)
			return Collections.emptyList();
		long methodId = setup.getImpactMethod().getId();
		String query = "select m.parameters from ImpactMethod m where "
				+ "m.id = :methodId";
		ParameterDao dao = new ParameterDao(database);
		List<Parameter> allParams = dao.getAll(query,
				Collections.singletonMap("methodId", methodId));
		List<Parameter> shapeFileParams = new ArrayList<>();
		for (Parameter param : allParams) {
			if (param.getExternalSource() != null
					&& "SHAPE_FILE".equals(param.getSourceType()))
				shapeFileParams.add(param);
		}
		return shapeFileParams;
	}

	private ContributionResult calcRegioResult(ContributionResult baseResult) {
		ContributionResult regioResult = initRegioResult(baseResult);
		ParameterSet set = ParameterSet.calculate(features,
				shapeFileParameters, repository);
		ProductIndex index = baseResult.getProductIndex();
		IMatrix impactResultMatrix = regioResult.getSingleImpactResults();
		for (int i = 0; i < index.size(); i++) {
			LongPair processProduct = index.getProductAt(i);
			if (!features.containsKey(processProduct))
				continue;
			Map<String, Double> params = set.get(processProduct);
			ImpactMatrix regioImpacts = createRegioImpacts(params);
			double[] flowResults = baseResult.getSingleFlowResults().getColumn(
					i);
			double[] impactResults = solver.multiply(
					regioImpacts.getFactorMatrix(), flowResults);
			for (int row = 0; row < impactResults.length; row++)
				impactResultMatrix.setEntry(row, i, impactResults[row]);
		}
		calcTotalImpactResult(regioResult);
		return regioResult;
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
}
