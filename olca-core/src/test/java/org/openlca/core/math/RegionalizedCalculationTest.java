package org.openlca.core.math;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.matrix.FastMatrixBuilder;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.FullResult;

/**
 * We test different aspects of regionalized models here. Our test model has two
 * connected processes `p1` and `p2` where `p1` is the reference process of the
 * model with an input of 2 units of `p2`:
 *
 * <code>
 * A = [ 1.0  0.0 ; -2.0  1.0 ]
 * f = [ 1.0 ; 0.0 ]
 * s = A \ f   # = [ 1 ; 2 ]
 * </code>
 * <p>
 * Process `p1` has emissions of 2 units of a flow `e1` and process `p2` has
 * emissions of 2 units of a flow `e2`. Also, we define two locations `loc1` and
 * `loc2` and a regionalized LCIA method with the following factors:
 *
 * <code>
 * e1 loc1 9.0
 * e1 loc2 6.0
 * e1 ____ 3.0
 * e2 loc1 6.0
 * e2 loc2 4.0
 * e2 ____ 2.0
 * </code>
 * <p>
 * Then we assign different combinations where we assign the locations to the
 * processes and exchanges and compare the calculated results.
 */
public class RegionalizedCalculationTest {

	private IDatabase db = Tests.getDb();

	private Process p1;
	private Process p2;
	private Flow e1;
	private Flow e2;
	private Location loc1;
	private Location loc2;
	private Location loc3;
	private ProductSystem sys;
	private ImpactCategory impact;
	private ImpactMethod method;

	@Before
	public void setup() {
		loc1 = location("L1");
		loc2 = location("L2");
		loc3 = location("L3");
		e1 = flow("e1", "kg", FlowType.ELEMENTARY_FLOW);
		e2 = flow("e2", "kg", FlowType.ELEMENTARY_FLOW);

		// process p2
		p2 = new Process();
		Flow pp2 = flow("p2", "kg", FlowType.PRODUCT_FLOW);
		p2.quantitativeReference = p2.output(pp2, 1);
		p2.output(e2, 2.0);
		p2 = new ProcessDao(db).insert(p2);

		// process p1
		p1 = new Process();
		Flow pp1 = flow("p1", "kg", FlowType.PRODUCT_FLOW);
		p1.quantitativeReference = p1.output(pp1, 1.0);
		p1.input(pp2, 2.0);
		p1.output(e1, 2.0);
		p1 = new ProcessDao(db).insert(p1);

		// create the product system
		sys = ProductSystem.of(p1);
		sys.processes.add(p2.id);
		ProcessLink link = new ProcessLink();
		link.providerId = p2.id;
		link.processId = p1.id;
		link.flowId = pp2.id;
		link.exchangeId = exchange(p1, pp2).id;
		sys.processLinks.add(link);
		sys = new ProductSystemDao(db).insert(sys);

		// create the LCIA method
		impact = new ImpactCategory();
		Object[][] factors = new Object[][]{
				{e1, loc1, 9.0},
				{e1, loc2, 6.0},
				{e1, null, 3.0},
				{e2, loc1, 6.0},
				{e2, loc2, 4.0},
				{e2, null, 2.0},
		};
		Arrays.stream(factors).forEach(row -> {
			var f = impact.factor((Flow) row[0], (Double) row[2]);
			f.location = (Location) row[1];
		});
		impact = new ImpactCategoryDao(db).insert(impact);
		method = new ImpactMethod();
		method.impactCategories.add(impact);
		method = new ImpactMethodDao(db).insert(method);

	}

	@After
	public void tearDown() {
		Tests.clearDb();
	}

	@Test
	public void checkNormalCalculation() {
		CalculationSetup setup = calcSetup();
		SystemCalculator calc = new SystemCalculator(db, new JavaSolver());
		FullResult r = calc.calculateFull(setup);

		// total results
		checkTotalFlowResults(r, new Object[][]{
				{e1, 2.0},
				{e2, 4.0},
		});
		checkTotalImpactResult(r, 14.0);

		// direct contributions
		checkDirectFlowResults(r, new Object[][]{
				{p1, e1, 2.0},
				{p1, e2, 0.0},
				{p2, e1, 0.0},
				{p2, e2, 4.0},
		});
		checkDirectImpactResults(r, new Object[][]{
				{p1, 2 * 3.0},
				{p2, 4 * 2.0},
		});

		// upstream contributions
		checkUpstreamFlowResults(r, new Object[][]{
				{p1, e1, 2.0},
				{p1, e2, 4.0},
				{p2, e1, 0.0},
				{p2, e2, 4.0},
		});
		checkUpstreamImpactResults(r, new Object[][]{
				{p1, 14.0},
				{p2, 8.0},
		});
	}

	@Test
	public void checkNoLocations() {
		CalculationSetup setup = calcSetup();
		SystemCalculator calc = new SystemCalculator(
				db, new JavaSolver());
		FullResult r = calc.calculateFull(setup);

		// total results
		checkTotalFlowResults(r, new Object[][]{
				{e1, 2.0},
				{e2, 4.0},
		});
		checkTotalImpactResult(r, 14.0);

		// direct contributions
		checkDirectFlowResults(r, new Object[][]{
				{p1, e1, 2.0},
				{p1, e2, 0.0},
				{p2, e1, 0.0},
				{p2, e2, 4.0},
		});
		checkDirectImpactResults(r, new Object[][]{
				{p1, 2 * 3.0},
				{p2, 4 * 2.0},
		});

		// upstream contributions
		checkUpstreamFlowResults(r, new Object[][]{
				{p1, e1, 2.0},
				{p1, e2, 4.0},
				{p2, e1, 0.0},
				{p2, e2, 4.0},
		});
		checkUpstreamImpactResults(r, new Object[][]{
				{p1, 14.0},
				{p2, 8.0},
		});
	}

	/**
	 * When there is no characterization factor available for a
	 * specific flow-location pair, the default characterization
	 * factor should be taken in this case. The default
	 * characterization factor is the factor where no location
	 * is assigned.
	 */
	@Test
	public void testDefaultCharacterization() {
		p1 = setLoc(p1, loc3);
		p2 = setLoc(p2, loc3);
		CalculationSetup setup = calcSetup();
		setup.withRegionalization = true;
		SystemCalculator calc = new SystemCalculator(db, new JavaSolver());
		FullResult r = calc.calculateFull(setup);

		// LCI results and contributions
		checkRegTotalFlowResults(r, new Object[][]{
				{e1, loc3, 2.0},
				{e2, loc3, 4.0},
		});
		checkDirectFlowResults(r, new Object[][]{
				{p1, e1, loc3, 2.0},
				{p1, e2, loc3, 0.0},
				{p2, e1, loc3, 0.0},
				{p2, e2, loc3, 4.0},
		});
		checkUpstreamFlowResults(r, new Object[][]{
				{p1, e1, loc3, 2.0},
				{p1, e2, loc3, 4.0},
				{p2, e1, loc3, 0.0},
				{p2, e2, loc3, 4.0},
		});

		// check LCIA results
		checkTotalImpactResult(r, 14.0);
		checkDirectImpactResults(r, new Object[][]{
				{p1, 2 * 3.0},
				{p2, 4 * 2.0},
		});
		checkUpstreamImpactResults(r, new Object[][]{
				{p1, 14.0},
				{p2, 8.0},
		});
	}

	/**
	 * If no location is specified on a elementary flow, the process location is
	 * taken by default for that flow if available.
	 */
	@Test
	public void testProcessLocations() {
		p1 = setLoc(p1, loc1);
		p2 = setLoc(p2, loc2);
		CalculationSetup setup = calcSetup();
		setup.withRegionalization = true;
		SystemCalculator calc = new SystemCalculator(db, new JavaSolver());
		FullResult r = calc.calculateFull(setup);
		checkRegionalizedResults(r);
	}

	@Test
	public void testFastMatrixBuilderProcesssLocations() {
		p1 = setLoc(p1, loc1);
		p2 = setLoc(p2, loc2);
		CalculationSetup setup = calcSetup();
		setup.withRegionalization = true;
		FastMatrixBuilder builder = new FastMatrixBuilder(db, setup);
		MatrixData data = builder.build();
		LcaCalculator calculator = new LcaCalculator(new JavaSolver(), data);
		FullResult r = calculator.calculateFull();
		checkRegionalizedResults(r);
	}

	private void checkRegionalizedResults(FullResult r) {

		checkRegTotalFlowResults(r, new Object[][]{
				{e1, loc1, 2.0},
				{e1, loc2, 0.0},
				{e1, null, 0.0},
				{e2, loc1, 0.0},
				{e2, loc2, 4.0},
				{e2, null, 0.0},
		});

		checkTotalImpactResult(r, 2 * 9.0 + 4 * 4.0);

		checkDirectFlowResults(r, new Object[][]{
				{p1, e1, loc1, 2.0},
				{p1, e1, loc2, 0.0},
				{p1, e1, null, 0.0},
				{p1, e2, loc1, 0.0},
				{p1, e2, loc2, 0.0},
				{p1, e2, null, 0.0},
				{p2, e1, loc1, 0.0},
				{p2, e1, loc2, 0.0},
				{p2, e1, null, 0.0},
				{p2, e2, loc1, 0.0},
				{p2, e2, loc2, 4.0},
				{p2, e2, null, 0.0},
		});

		checkDirectImpactResults(r, new Object[][]{
				{p1, 2.0 * 9.0},
				{p2, 4.0 * 4.0},
		});

		checkUpstreamFlowResults(r, new Object[][]{
				{p1, e1, loc1, 2.0},
				{p1, e1, loc2, 0.0},
				{p1, e1, null, 0.0},
				{p1, e2, loc1, 0.0},
				{p1, e2, loc2, 4.0},
				{p1, e2, null, 0.0},
				{p2, e1, loc1, 0.0},
				{p2, e1, loc2, 0.0},
				{p2, e1, null, 0.0},
				{p2, e2, loc1, 0.0},
				{p2, e2, loc2, 4.0},
				{p2, e2, null, 0.0},
		});

		checkUpstreamImpactResults(r, new Object[][]{
				{p1, 2 * 9.0 + 4 * 4.0},
				{p2, 4.0 * 4.0},
		});
	}

	/**
	 * Checks a sequence of (Flow, Double) values.
	 */
	private void checkTotalFlowResults(FullResult r, Object[][] defs) {
		for (Object[] row : defs) {
			Flow flow = (Flow) row[0];
			int flowIdx = r.flowIndex.of(flow.id);
			IndexFlow iFlow = r.flowIndex.at(flowIdx);
			double v = r.getTotalFlowResult(iFlow);
			Assert.assertEquals((Double) row[1], v, 1e-10);
		}
	}

	/**
	 * Checks a sequence of (Flow, Location, Double) values.
	 */
	private void checkRegTotalFlowResults(FullResult r, Object[][] defs) {
		for (Object[] row : defs) {
			Flow flow = (Flow) row[0];
			Location loc = (Location) row[1];
			int flowIdx = r.flowIndex.of(flow.id, loc != null ? loc.id : 0L);
			IndexFlow iFlow = r.flowIndex.at(flowIdx);
			double v = r.getTotalFlowResult(iFlow);
			Assert.assertEquals((Double) row[2], v, 1e-10);
		}
	}

	/**
	 * Checks a sequence of (Process, Flow, Double) for non-regionalized results
	 * and (Process, Flow, Location, Double) for regionalized results.
	 */
	private void checkDirectFlowResults(FullResult r, Object[][] defs) {
		for (Object[] row : defs) {
			ProcessProduct product = product((Process) row[0]);
			Flow flow = (Flow) row[1];

			if (row[2] instanceof Number) {
				// non-regionalized
				int flowIdx = r.flowIndex.of(flow.id);
				IndexFlow iFlow = r.flowIndex.at(flowIdx);
				double v = r.getDirectFlowResult(product, iFlow);
				Assert.assertEquals((Double) row[2], v, 1e-10);
			} else {
				// regionalized
				Location loc = (Location) row[2];
				int flowIdx = r.flowIndex.of(flow.id,
						loc != null ? loc.id : 0L);
				IndexFlow iFlow = r.flowIndex.at(flowIdx);
				double v = r.getDirectFlowResult(product, iFlow);
				Assert.assertEquals((Double) row[3], v, 1e-10);
			}
		}
	}

	/**
	 * Checks a sequence of (Process, Flow, Double) for non-regionalized results
	 * and (Process, Flow, Location, Double) for regionalized results.
	 */
	private void checkUpstreamFlowResults(FullResult r, Object[][] defs) {
		for (Object[] row : defs) {
			ProcessProduct product = product((Process) row[0]);
			Flow flow = (Flow) row[1];

			if (row[2] instanceof Number) {
				// non-regionalized
				int flowIdx = r.flowIndex.of(flow.id);
				IndexFlow iFlow = r.flowIndex.at(flowIdx);
				double v = r.getUpstreamFlowResult(product, iFlow);
				Assert.assertEquals((Double) row[2], v, 1e-10);
			} else {
				// regionalized
				Location loc = (Location) row[2];
				int flowIdx = r.flowIndex.of(flow.id,
						loc != null ? loc.id : 0L);
				IndexFlow iFlow = r.flowIndex.at(flowIdx);
				double v = r.getUpstreamFlowResult(product, iFlow);
				Assert.assertEquals((Double) row[3], v, 1e-10);
			}
		}
	}

	private void checkTotalImpactResult(FullResult r, double val) {
		Assert.assertEquals(val, r.getTotalImpactResult(des(impact)), 1e-10);
	}

	private void checkDirectImpactResults(FullResult r, Object[][] defs) {
		for (Object[] row : defs) {
			double v = r.getDirectImpactResult(
					product((Process) row[0]), des(impact));
			Assert.assertEquals((Double) row[1], v, 1e-10);
		}
	}

	private void checkUpstreamImpactResults(FullResult r, Object[][] defs) {
		for (Object[] row : defs) {
			double v = r.getUpstreamImpactResult(
					product((Process) row[0]), des(impact));
			Assert.assertEquals((Double) row[1], v, 1e-10);
		}
	}

	private ProcessProduct product(Process p) {
		Flow pp = p.quantitativeReference.flow;
		return ProcessProduct.of(p, pp);
	}

	private CalculationSetup calcSetup() {
		// reload the product system to get the updates
		sys = new ProductSystemDao(db).getForId(sys.id);
		CalculationSetup setup = new CalculationSetup(sys);
		setup.impactMethod = Descriptor.of(method);
		return setup;
	}

	@Test
	public void testRegionalizedCalculation() {

		Flow nox = flow("NOx", "mg", FlowType.ELEMENTARY_FLOW);
		Location loc1 = location("L1");
		Location loc2 = location("L2");

		// create the process
		Process p = new Process();
		p.name = "transport, bus";
		p.quantitativeReference = p.output(flow(
				"transport, bus", "p*km", FlowType.PRODUCT_FLOW), 1);
		Exchange e1 = p.output(nox, 5);
		e1.location = loc1;
		Exchange e2 = p.output(nox, 10);
		e2.location = loc2;
		p = new ProcessDao(db).insert(p);

		// create the LCIA category & method
		var impact = new ImpactCategory();
		impact.name = "human tox";
		impact.factor(nox, 0.5);
		impact.factor(nox, 0.1).location = loc1;
		impact.factor(nox, 0.9).location = loc2;
		impact = new ImpactCategoryDao(db).insert(impact);
		var method = new ImpactMethod();
		method.impactCategories.add(impact);
		method = new ImpactMethodDao(db).insert(method);

		// create the product system and calculation setup
		CalculationSetup setup = new CalculationSetup(ProductSystem.of(p));
		setup.withRegionalization = true;
		setup.impactMethod = Descriptor.of(method);
		SystemCalculator calculator = new SystemCalculator(db, new JavaSolver());

		FullResult r = calculator.calculateFull(setup);
		Assert.assertTrue(r.flowIndex.isRegionalized);
		checkRegTotalFlowResults(r, new Object[][]{
				{nox, loc1, 5.0},
				{nox, loc2, 10.0},
		});
	}

	private Flow flow(String name, String unit, FlowType type) {
		FlowDao dao = new FlowDao(db);
		List<Flow> flows = dao.getForName(name);
		if (!flows.isEmpty())
			return flows.get(0);
		var property = property(unit);
		Flow flow = Flow.of(name, type, property);
		return dao.insert(flow);
	}

	private FlowProperty property(String unit) {
		FlowPropertyDao dao = new FlowPropertyDao(db);
		List<FlowProperty> props = dao.getForName(unit);
		if (!props.isEmpty())
			return props.get(0);
		FlowProperty prop = new FlowProperty();
		prop.name = unit;
		prop.unitGroup = unitGroup(unit);
		return dao.insert(prop);
	}

	private UnitGroup unitGroup(String unit) {
		var dao = new UnitGroupDao(db);
		var groups = dao.getForName(unit);
		if (!groups.isEmpty())
			return groups.get(0);
		var group = UnitGroup.of(unit, Unit.of(unit));
		return dao.insert(group);
	}

	private Location location(String code) {
		LocationDao dao = new LocationDao(db);
		Location loc = dao.getForRefId(code);
		if (loc != null)
			return loc;
		loc = new Location();
		loc.refId = code;
		loc.code = code;
		loc.name = code;
		return dao.insert(loc);
	}

	private Exchange exchange(Process p, Flow f) {
		return p.exchanges.stream()
				.filter(e -> f.equals(e.flow))
				.findFirst()
				.orElse(null);
	}

	private ImpactCategoryDescriptor des(ImpactCategory imp) {
		return Descriptor.of(imp);
	}

	private Process setLoc(Process p, Location loc) {
		p.location = loc;
		return new ProcessDao(db).update(p);
	}
}
