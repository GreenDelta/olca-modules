package org.openlca.geo;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.CalculationType;
import org.openlca.core.math.DataStructures;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.matrix.DIndex;
import org.openlca.core.matrix.InventoryConfig;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.expressions.FormulaInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final IDatabase db;
	private final IMatrixSolver solver;

	public RegCalculator(IDatabase db, IMatrixSolver solver) {
		this.db = db;
		this.solver = solver;
	}

	public SimpleResult calculateSimple(CalculationSetup setup) {
		log.trace("calculate simple regionalized result");
		return calculator(setup).calculateSimple();
	}

	public ContributionResult calculateContributions(CalculationSetup setup) {
		log.trace("calculate regionalized contribution result");
		return calculator(setup).calculateContributions();
	}

	public FullResult calculateFull(CalculationSetup setup) {
		log.trace("calculate regionalized full result");
		return calculator(setup).calculateFull();
	}

	private LcaCalculator calculator(CalculationSetup setup) {
		MatrixData data;
		// TODO: the direct network calculation does not yet work here
		// if (setup.productSystem.withoutNetwork) {
		// 	data = new FastMatrixBuilder(db, setup).build();
		// }  else {

		Map<ProcessProduct, SimpleResult> subs = calculateSubSystems(setup);
		data = matrixData(setup, db, subs);
		return new LcaCalculator(solver, data);
	}

	private Map<ProcessProduct, SimpleResult> calculateSubSystems(
			CalculationSetup setup) {
		if (setup == null || setup.productSystem == null)
			return Collections.emptyMap();

		// collect the sub-systems
		HashSet<ProcessProduct> subSystems = new HashSet<>();
		ProductSystemDao sysDao = new ProductSystemDao(db);
		FlowDao flowDao = new FlowDao(db);
		for (ProcessLink link : setup.productSystem.processLinks) {
			if (!link.isSystemLink)
				continue;
			ProductSystemDescriptor sys = sysDao.getDescriptor(link.providerId);
			FlowDescriptor flow = flowDao.getDescriptor(link.flowId);
			if (sys == null || flow == null) {
				log.error("could not load descriptors of system link {}", link);
				continue;
			}
			subSystems.add(ProcessProduct.of(sys, flow));
		}
		if (subSystems.isEmpty())
			return Collections.emptyMap();

		// calculate the LCI results of the sub-systems
		HashMap<ProcessProduct, SimpleResult> map = new HashMap<>();
		for (ProcessProduct pp : subSystems) {
			ProductSystem sys = sysDao.getForId(pp.id());
			if (sys == null)
				continue;
			CalculationSetup subSetup = new CalculationSetup(setup.type, sys);
			subSetup.parameterRedefs.addAll(sys.parameterRedefs);
			subSetup.withCosts = setup.withCosts;
			subSetup.allocationMethod = setup.allocationMethod;
			SimpleResult r = calculateSimple(subSetup);
			map.put(pp, r);
		}
		return map;
	}

	private MatrixData matrixData(
			CalculationSetup setup,
			IDatabase db,
			Map<ProcessProduct, SimpleResult> subResults) {

		TechIndex techIndex = DataStructures.createProductIndex(
				setup.productSystem, db);
		techIndex.setDemand(setup.getDemandValue());
		FormulaInterpreter interpreter = DataStructures.interpreter(
				db, setup, techIndex);

		InventoryConfig conf = new InventoryConfig(db, techIndex);
		conf.allocationMethod = setup.allocationMethod;
		conf.interpreter = interpreter;
		conf.subResults = subResults;
		conf.withCosts = setup.withCosts;
		conf.withUncertainties = setup.type == CalculationType.MONTE_CARLO_SIMULATION;
		RegInventoryBuilder builder = new RegInventoryBuilder(conf);
		MatrixData data = builder.build();

		// add the LCIA matrix structures
		if (setup.impactMethod != null) {
			DIndex<ImpactCategoryDescriptor> impactIdx = new DIndex<>();
			new ImpactMethodDao(db).getCategoryDescriptors(
					setup.impactMethod.id).forEach(impactIdx::put);
			if (!impactIdx.isEmpty()) {
				RegImpactBuilder ib = new RegImpactBuilder(db);
				ib.withUncertainties(conf.withUncertainties);
				RegImpactBuilder.RegImpactData idata = ib.build(
						data.flowIndex, impactIdx, interpreter);
				data.impactMatrix = idata.impactMatrix;
				data.impactIndex = impactIdx;
				data.impactUncertainties = idata.impactUncertainties;
			}
		}
		return data;
	}
}
