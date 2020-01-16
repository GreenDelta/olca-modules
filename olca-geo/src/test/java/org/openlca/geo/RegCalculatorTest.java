package org.openlca.geo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.CalculationType;
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
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.results.SimpleResult;

import java.util.List;
import java.util.UUID;

public class RegCalculatorTest {

	private IDatabase db = Tests.getDb();

	@After
	public void tearDown() {
		Tests.clearDb();
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
}
