package org.openlca.io.olca;

import java.util.List;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;

/**
 * Tests that references between entities a correctly set in a database import.
 * We typically ignore that test because it takes very long, but it should be
 * enabled after model- or database-updates. We do not check category references
 * here as this is already covered in another test.
 */
// @Ignore
public class RefsTest {

	private static IDatabase db;
	private static IDatabase target;

	@BeforeClass
	public static void setup() {
		db = Derby.createInMemory();
		target = Derby.createInMemory();

		// unit group and flow property with circular link
		var units = UnitGroup.of("units", "kg");
		var mass = FlowProperty.of("mass", units);
		db.insert(units, mass);
		units.defaultFlowProperty = mass;
		units = db.update(units);
		mass = db.get(FlowProperty.class, mass.id);

		// currency, location, flows
		var eur = Currency.of("eur");
		eur.referenceCurrency = eur;
		var loc = Location.of("loc");
		var p = Flow.product("p", mass);
		var q = Flow.product("q", mass);
		var e = Flow.elementary("e", mass);
		db.insert(eur, loc, p, q, e);

		// actor, source, parameter, social indicator
		var actor = Actor.of("actor");
		var source = Source.of("source");
		var param = Parameter.global("param", 42);
		var social = SocialIndicator.of("social", mass);
		db.insert(actor, source, param, social);

		// impact category
		var impact = ImpactCategory.of("impact");
		impact.source = source;
		var factor = impact.factor(e, 1);
		factor.location = loc;
		db.insert(impact);

		// impact method
		var method = ImpactMethod.of("method");
		method.source = source;
		method.impactCategories.add(impact);
		var nws = NwSet.of("nws");
		method.add(nws);
		nws.add(NwFactor.of(impact, 1,1));
		db.insert(method);

		new DatabaseImport(db, target).run();
	}

	@AfterClass
	public static void cleanup() throws Exception {
		db.close();
		target.close();
	}

	@Test
	public void testExists() {
		get(UnitGroup.class, "units");
		get(FlowProperty.class, "mass");
		get(Currency.class, "eur");
		get(Location.class, "loc");
		get(Flow.class, "p");
		get(Flow.class, "q");
		get(Flow.class, "e");
		get(Actor.class, "actor");
		get(Source.class, "source");
		get(Parameter.class, "param");
		get(SocialIndicator.class, "social");
		get(ImpactCategory.class, "impact");
		get(ImpactMethod.class, "method");
	}

	@Test
	public void testUnits() {
		var units = get(UnitGroup.class, "units");
		check(units.referenceUnit, "kg");
		check(units.units.get(0), "kg");
		var mass = get(FlowProperty.class, "mass");
		check(mass.unitGroup, "units");
		check(units.defaultFlowProperty, "mass");
	}

	@Test
	public void testCurrency() {
		var eur = get(Currency.class, "eur");
		check(eur.referenceCurrency, "eur");
	}

	@Test
	public void testSocialIndicator() {
		var social = get(SocialIndicator.class, "social");
		check(social.activityQuantity, "mass");
		check(social.activityUnit, "kg");
	}

	@Test
	public void testFlows() {
		var flows = List.of(
				get(Flow.class, "p"),
				get(Flow.class, "q"),
				get(Flow.class, "e"));
		for (var flow : flows) {
			check(flow.referenceFlowProperty, "mass");
			check(flow.flowPropertyFactors.get(0).flowProperty, "mass");
		}
	}

	@Test
	public void testImpactCategory() {
		var impact = get(ImpactCategory.class, "impact");
		check(impact.source, "source");
		var factor = impact.impactFactors.get(0);
		check(factor.flow, "e");
		check(factor.flowPropertyFactor.flowProperty, "mass");
		check(factor.unit, "kg");
		check(factor.location, "loc");
	}

	@Test
	public void testImpactMethod() {
		var method = get(ImpactMethod.class, "method");
		check(method.source, "source");
		check(method.impactCategories.get(0), "impact");
		var nws = method.nwSets.get(0);
		check(nws, "nws");
		check(nws.factors.get(0).impactCategory, "impact");
	}

	private <T extends RootEntity> T get(Class<T> type, String name) {
		return check(target.getForName(type, name), name);
	}

	private <T extends RefEntity> T check(T e, String name) {
		assertNotNull(e);
		assertEquals(name, e.name);
		return e;
	}
}
