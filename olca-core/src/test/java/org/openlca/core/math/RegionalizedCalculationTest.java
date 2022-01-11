package org.openlca.core.math;

import java.util.Arrays;
import java.util.List;
import java.util.function.ToDoubleFunction;

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
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
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

	private final IDatabase db = Tests.getDb();

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

		// locations
		loc1 = db.insert(Location.of("L1"));
		loc2 = db.insert(Location.of("L2"));
		loc3 = db.insert(Location.of("L3"));

		// quantities and flows
		var units = db.insert(UnitGroup.of("Units of mass", "kg"));
		var mass = db.insert(FlowProperty.of("Mass", units));
		e1 = db.insert(Flow.elementary("e1", mass));
		e2 = db.insert(Flow.elementary("e2", mass));
		var pp1 = db.insert(Flow.product("p1", mass));
		var pp2 = db.insert(Flow.product("p2", mass));

		// process p2
		p2 = Process.of("p2", pp2);
		p2.output(e2, 2.0);
		p2 = db.insert(p2);

		// process p1
		p1 = Process.of("p1", pp1);
		p1.input(pp2, 2.0);
		p1.output(e1, 2.0);
		p1 = db.insert(p1);

		// create the product system
		sys = ProductSystem.of(p1);
		sys.link(p2, p1);
		sys = db.insert(sys);

		// create the LCIA method
		impact = ImpactCategory.of("Impacts");
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
		impact = db.insert(impact);
		method = ImpactMethod.of("Method");
		method.impactCategories.add(impact);
		method = db.insert(method);
	}

	@After
	public void tearDown() {
		db.clear();
	}

	@Test
	public void checkNormalCalculation() {
		var r = FullResult.of(db, calcSetup());

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
		SystemCalculator calc = new SystemCalculator(db);
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
		var setup = calcSetup().withRegionalization(true);
		var calc = new SystemCalculator(db);
		var r = calc.calculateFull(setup);

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
	 * If no location is specified for an elementary flow, the process location is
	 * taken by default for that flow if available.
	 */
	@Test
	public void testProcessLocations() {
		p1 = setLoc(p1, loc1);
		p2 = setLoc(p2, loc2);
		var setup = calcSetup().withRegionalization(true);
		var calc = new SystemCalculator(db);
		var r = calc.calculateFull(setup);
		checkRegionalizedResults(r);
	}

	@Test
	public void testMatrixBuilderProcesssLocations() {
		p1 = setLoc(p1, loc1);
		p2 = setLoc(p2, loc2);
		var setup = calcSetup().withRegionalization(true);
		var data = MatrixData.of(db, TechIndex.of(db, setup))
			.withSetup(setup)
			.build();
		var result = FullResult.of(db, data);
		checkRegionalizedResults(result);
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
			int flowIdx = r.enviIndex().of(flow.id);
			EnviFlow iFlow = r.enviIndex().at(flowIdx);
			double v = r.getTotalFlowResult(iFlow);
			Assert.assertEquals((Double) row[1], v, 1e-10);
		}
	}

	/**
	 * Checks a sequence of (Flow, Location, Double) values.
	 */
	private void checkRegTotalFlowResults(FullResult r, Object[][] defs) {
		for (Object[] row : defs) {
			var flow = (Flow) row[0];
			var loc = (Location) row[1];
			int flowIdx = r.enviIndex().of(flow.id, loc != null ? loc.id : 0L);
			double value = flowIdx < 0
				? 0.0
				: r.getTotalFlowResult(r.enviIndex().at(flowIdx));
			Assert.assertEquals((Double) row[2], value, 1e-10);
		}
	}

	/**
	 * Checks a sequence of (Process, Flow, Double) for non-regionalized results
	 * and (Process, Flow, Location, Double) for regionalized results.
	 */
	private void checkDirectFlowResults(FullResult r, Object[][] defs) {
		for (Object[] row : defs) {
			TechFlow product = product((Process) row[0]);
			Flow flow = (Flow) row[1];

			if (row[2] instanceof Number) {
				// non-regionalized
				int flowIdx = r.enviIndex().of(flow.id);
				EnviFlow iFlow = r.enviIndex().at(flowIdx);
				double v = r.getDirectFlowResult(product, iFlow);
				Assert.assertEquals((Double) row[2], v, 1e-10);
			} else {
				// regionalized
				double v = orZero(flow, (Location) row[2], r.enviIndex(),
					iFlow -> r.getDirectFlowResult(product, iFlow));
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
			TechFlow product = product((Process) row[0]);
			Flow flow = (Flow) row[1];

			if (row[2] instanceof Number) {
				// non-regionalized
				int flowIdx = r.enviIndex().of(flow.id);
				var iFlow = r.enviIndex().at(flowIdx);
				double v = r.getUpstreamFlowResult(product, iFlow);
				Assert.assertEquals((Double) row[2], v, 1e-10);
			} else {
				// regionalized
				double v = orZero(flow, (Location) row[2], r.enviIndex(),
					iFlow -> r.getUpstreamFlowResult(product, iFlow));
				Assert.assertEquals((Double) row[3], v, 1e-10);
			}
		}
	}

	private double orZero(Flow flow, Location location, EnviIndex index,
		ToDoubleFunction<EnviFlow> fn) {
		if (flow == null)
			return 0;
		int idx = index.of(flow.id, location != null ? location.id : 0L);
		return idx >= 0
			? fn.applyAsDouble(index.at(idx))
			: 0;
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

	private TechFlow product(Process p) {
		Flow pp = p.quantitativeReference.flow;
		return TechFlow.of(p, pp);
	}

	private CalculationSetup calcSetup() {
		// reload the product system to get the updates
		sys = new ProductSystemDao(db).getForId(sys.id);
		return CalculationSetup.fullAnalysis(sys)
			.withImpactMethod(method);
	}

	@Test
	public void testRegionalizedCalculation() {

		Flow nox = flow("NOx", "mg", FlowType.ELEMENTARY_FLOW);

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
		var setup = CalculationSetup.fullAnalysis(ProductSystem.of(p))
			.withImpactMethod(method)
			.withRegionalization(true);
		var calculator = new SystemCalculator(db);

		FullResult r = calculator.calculateFull(setup);
		Assert.assertTrue(r.enviIndex().isRegionalized());
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

	private ImpactDescriptor des(ImpactCategory imp) {
		return Descriptor.of(imp);
	}

	private Process setLoc(Process p, Location loc) {
		p.location = loc;
		return new ProcessDao(db).update(p);
	}
}
