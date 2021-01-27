package org.openlca.core.math;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.FastMatrixBuilder;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.ProjectResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates the results of a calculation setup or project. The same calculator
 * can be used for different setups. The product systems of the setups may
 * contain sub-systems. The calculator does not check if there are obvious
 * errors like sub-system cycles etc.
 */
public class SystemCalculator {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final IDatabase db;
	private final MatrixSolver solver;
	private LibraryDir libDir;

	public SystemCalculator(IDatabase db) {
		this(db, Julia.isLoaded()
			? new JuliaSolver()
			: new JavaSolver());
	}

	public SystemCalculator(IDatabase db, MatrixSolver solver) {
		this.db = db;
		this.solver = solver;
	}

	public SystemCalculator withLibraries(LibraryDir libDir) {
		this.libDir = libDir;
		return this;
	}

	public SimpleResult calculateSimple(CalculationSetup setup) {
		log.trace("calculate product system - simple result");
		return calculator(setup).calculateSimple();
	}

	public ContributionResult calculateContributions(CalculationSetup setup) {
		log.trace("calculate product system - contribution result");
		return calculator(setup).calculateContributions();
	}

	public FullResult calculateFull(CalculationSetup setup) {
		log.trace("calculate product system - full result");
		return calculator(setup).calculateFull();
	}

	public ProjectResult calculate(Project project) {
		ProjectResult result = new ProjectResult();
		if (project == null)
			return result;

		var method = project.impactMethod != null
				? Descriptor.of(project.impactMethod)
				: null;
		var nwSet = project.nwSet != null
				? Descriptor.of(project.nwSet)
				: null;

		// calculate the project variants
		for (ProjectVariant v : project.variants) {
			if (v.isDisabled)
				continue;
			var setup = new CalculationSetup(v.productSystem);
			setup.setUnit(v.unit);
			setup.setFlowPropertyFactor(v.flowPropertyFactor);
			setup.setAmount(v.amount);
			setup.allocationMethod = v.allocationMethod;
			setup.impactMethod = method;
			setup.nwSet = nwSet;
			setup.parameterRedefs.addAll(v.parameterRedefs);
			setup.withCosts = true;
			// TODO: how to handle regionalization here?
			ContributionResult cr = calculateContributions(setup);
			result.addResult(v, cr);
		}
		return result;
	}

	private LcaCalculator calculator(CalculationSetup setup) {
		MatrixData data;
		if (setup.productSystem.withoutNetwork) {
			data = new FastMatrixBuilder(db, setup).build();
		} else {
			var subs = calculateSubSystems(setup);
			data = DataStructures.matrixData(db, setup, subs);
		}
		var calculator = new LcaCalculator(solver, data);
		if (libDir != null) {
			calculator.withLibraries(db, libDir);
		}
		return calculator;
	}

	/**
	 * Calculates (recursively) the sub-systems of the product system of the
	 * given setup. It returns an empty map when there are no subsystems.
	 */
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
			ProductSystem subSys = sysDao.getForId(pp.id());
			if (subSys == null)
				continue;
			var subSetup = new CalculationSetup(subSys);
			subSetup.parameterRedefs.addAll(setup.parameterRedefs);
			ParameterRedefs.addTo(subSetup, subSys);
			subSetup.withCosts = setup.withCosts;
			subSetup.withUncertainties = setup.withUncertainties;
			subSetup.withRegionalization = setup.withRegionalization;
			subSetup.allocationMethod = setup.allocationMethod;
			SimpleResult r = calculateSimple(subSetup);
			map.put(pp, r);
		}
		return map;
	}
}
