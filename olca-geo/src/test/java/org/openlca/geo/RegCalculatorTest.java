package org.openlca.geo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.CalculationType;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * We test different aspects of regionalized models here.
 * Our test model has two connected processes `p1` and `p2`
 * where `p1` is the reference process of the model with
 * an input of 2 units of `p2`:
 *
 * <code>
 * A = [ 1.0  0.0 ; -2.0  1.0 ]
 * f = [ 1.0 ; 0.0 ]
 * s = A \ f   # = [ 1 ; 2 ]
 * </code>
 * <p>
 * Process `p1` has emissions of 2 units of a flow `e1` and
 * process `p2` has emissions of 2 units of a flow `e2`. Also,
 * we define two locations `loc1` and `loc2` and a regionalized
 * LCIA method with the following factors:
 *
 * <code>
 * e1 loc1 7.0
 * e1 loc2 5.0
 * e1 ____ 3.0
 * e2 loc1 3.5
 * e2 loc2 5.5
 * e2 ____ 7.5
 * </code>
 * <p>
 * Then we assign different combinations where we assign the
 * locations to the processes and exchanges and compare the
 * calculated results.
 */
public class RegCalculatorTest {

	private IDatabase db = Tests.getDb();

	private Process p1;
	private Process p2;
	private Flow e1;
	private Flow e2;
	private Location loc1;
	private Location loc2;
	private ProductSystem sys;
	private ImpactCategory impact;
	private ImpactMethod method;

	@Before
	public void setup() {
		loc1 = location("L1");
		loc2 = location("L2");
		e1 = flow("e1", "kg", FlowType.ELEMENTARY_FLOW);
		e2 = flow("e2", "kg", FlowType.ELEMENTARY_FLOW);

		// process p2
		p2 = new Process();
		Flow pp2 = flow("p2", "kg", FlowType.PRODUCT_FLOW);
		with(p2.exchange(pp2), e -> {
			p2.quantitativeReference = e;
			e.isInput = false;
		});
		with(p2.exchange(e2), e -> {
			e.isInput = false;
			e.amount = 2.0;
		});
		p2 = new ProcessDao(db).insert(p2);

		// process p1
		p1 = new Process();
		Flow pp1 = flow("p1", "kg", FlowType.PRODUCT_FLOW);
		with(p1.exchange(pp1), e -> {
			p1.quantitativeReference = e;
			e.isInput = false;
		});
		with(p1.exchange(pp2), e -> {
			e.isInput = true;
			e.amount = 2.0;
		});
		with(p1.exchange(e1), e -> {
			e.isInput = false;
			e.amount = 2.0;
		});
		p1 = new ProcessDao(db).insert(p1);

		// create the product system
		sys = ProductSystem.from(p1);
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
				{e1, loc1, 7.0},
				{e1, loc2, 5.0},
				{e1, null, 3.0},
				{e2, loc1, 3.5},
				{e2, loc2, 5.5},
				{e2, null, 7.5},
		};
		Arrays.stream(factors).forEach(row -> {
			ImpactFactor f = impact.addFactor((Flow) row[0]);
			f.location = (Location) row[1];
			f.value = (Double) row[2];
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
		checkTotalImpactResult(r, 36.0);

		// direct contributions
		checkDirectFlowResults(r, new Object[][] {
				{p1, e1, 2.0},
				{p1, e2, 0.0},
				{p2, e1, 0.0},
				{p2, e2, 4.0},
		});
		checkDirectImpactResults(r, new Object[][]{
				{p1, 6.0},
				{p2, 30.0},
		});

		// upstream contributions
		checkUpstreamFlowResults(r, new Object[][] {
				{p1, e1, 2.0},
				{p1, e2, 4.0},
				{p2, e1, 0.0},
				{p2, e2, 4.0},
		});
		checkUpstreamImpactResults(r, new Object[][]{
				{p1, 36.0},
				{p2, 30.0},
		});

	}

	@Test
	public void checkNoLocations() {
		CalculationSetup setup = calcSetup();
		RegCalculator calc = new RegCalculator(db, new JavaSolver());
		FullResult r = calc.calculateFull(setup);

		// total results
		checkTotalFlowResults(r, new Object[][]{
				{e1, 2.0},
				{e2, 4.0},
		});
		checkTotalImpactResult(r, 36.0);

		// direct contributions
		checkDirectFlowResults(r, new Object[][] {
				{p1, e1, 2.0},
				{p1, e2, 0.0},
				{p2, e1, 0.0},
				{p2, e2, 4.0},
		});
		checkDirectImpactResults(r, new Object[][]{
				{p1, 6.0},
				{p2, 30.0},
		});

		// upstream contributions
		checkUpstreamFlowResults(r, new Object[][] {
				{p1, e1, 2.0},
				{p1, e2, 4.0},
				{p2, e1, 0.0},
				{p2, e2, 4.0},
		});
		checkUpstreamImpactResults(r, new Object[][]{
				{p1, 36.0},
				{p2, 30.0},
		});
	}

	private void checkTotalImpactResult(FullResult r, double val) {
		Assert.assertEquals(val, r.getTotalImpactResult(des(impact)), 1e-10);
	}

	private void checkTotalFlowResults(FullResult r, Object[][] defs) {
		Arrays.stream(defs).forEach(row -> {
			double v = r.getTotalFlowResult(des((Flow) row[0]));
			Assert.assertEquals((Double) row[1], v, 1e-10);
		});
	}

	private void checkDirectFlowResults(FullResult r, Object[][] defs) {
		Arrays.stream(defs).forEach(row -> {
			double v = r.getDirectFlowResult(
					product((Process) row[0]), des((Flow) row[1]));
			Assert.assertEquals((Double) row[2], v, 1e-10);
		});
	}

	private void checkUpstreamFlowResults(FullResult r, Object[][] defs) {
		Arrays.stream(defs).forEach(row -> {
			double v = r.getUpstreamFlowResult(
					product((Process) row[0]), des((Flow) row[1]));
			Assert.assertEquals((Double) row[2], v, 1e-10);
		});
	}

	private void checkDirectImpactResults(FullResult r, Object[][] defs) {
		for(Object[] row : defs) {
			double v = r.getDirectImpactResult(
					product((Process) row[0]), des(impact));
			Assert.assertEquals((Double) row[1], v, 1e-10);
		}
	}

	private void checkUpstreamImpactResults(FullResult r, Object[][] defs) {
		for(Object[] row : defs) {
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
		CalculationSetup setup = new CalculationSetup(
				CalculationType.CONTRIBUTION_ANALYSIS, sys);
		setup.impactMethod = Descriptors.toDescriptor(method);
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
		Exchange refFlow = p.exchange(flow(
				"transport, bus", "p*km", FlowType.PRODUCT_FLOW));
		p.quantitativeReference = refFlow;
		Exchange e1 = p.exchange(nox);
		e1.amount = 5;
		e1.location = loc1;
		Exchange e2 = p.exchange(nox);
		e2.amount = 10;
		e2.location = loc2;
		p = new ProcessDao(db).insert(p);

		// create the LCIA category & method
		ImpactCategory impact = new ImpactCategory();
		impact.name = "human tox";
		ImpactFactor i1 = impact.addFactor(nox);
		i1.value = 0.5; // the default factor
		ImpactFactor i2 = impact.addFactor(nox);
		i2.location = loc1;
		i2.value = 0.1;
		ImpactFactor i3 = impact.addFactor(nox);
		i3.location = loc2;
		i3.value = 0.9;
		impact = new ImpactCategoryDao(db).insert(impact);
		ImpactMethod method = new ImpactMethod();
		method.impactCategories.add(impact);
		method = new ImpactMethodDao(db).insert(method);

		// create the product system and calculation setup
		CalculationSetup setup = new CalculationSetup(
				CalculationType.CONTRIBUTION_ANALYSIS, ProductSystem.from(p));
		setup.impactMethod = Descriptors.toDescriptor(method);
		RegCalculator calculator = new RegCalculator(db, new JavaSolver());

		SimpleResult r = calculator.calculateSimple(setup);
		Assert.assertTrue(r.isRegionalized());
		Assert.assertEquals(5, r.getTotalFlowResult(
				Descriptors.toDescriptor(nox), Descriptors.toDescriptor(loc1)), 1e-10);
		Assert.assertEquals(10, r.getTotalFlowResult(
				Descriptors.toDescriptor(nox), Descriptors.toDescriptor(loc2)), 1e-10);
	}


	private Flow flow(String name, String unit, FlowType type) {
		FlowDao dao = new FlowDao(db);
		List<Flow> flows = dao.getForName(name);
		if (!flows.isEmpty())
			return flows.get(0);
		Flow flow = new Flow();
		flow.name = name;
		flow.refId = UUID.randomUUID().toString();
		flow.flowType = type;
		flow.addReferenceFactor(property(unit));
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
		UnitGroupDao dao = new UnitGroupDao(db);
		List<UnitGroup> groups = dao.getForName(unit);
		if (!groups.isEmpty())
			return groups.get(0);
		UnitGroup group = new UnitGroup();
		group.name = unit;
		group.addReferenceUnit(unit);
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

	private <T> void with(T t, Consumer<T> fn) {
		if (t != null) {
			fn.accept(t);
		}
	}

	private FlowDescriptor des(Flow flow) {
		return Descriptors.toDescriptor(flow);
	}

	private LocationDescriptor des(Location loc) {
		return Descriptors.toDescriptor(loc);
	}

	private ImpactCategoryDescriptor des(ImpactCategory imp) {
		return Descriptors.toDescriptor(imp);
	}
}
