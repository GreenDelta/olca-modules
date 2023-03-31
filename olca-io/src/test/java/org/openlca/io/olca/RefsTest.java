package org.openlca.io.olca;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialAspect;
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

		// actor, source, parameter, social indicator, dqs
		var actor = Actor.of("actor");
		var source = Source.of("source");
		var param = Parameter.global("param", 42);
		var social = SocialIndicator.of("social", mass);
		var dqs = DQSystem.of("dqs");
		dqs.source = source;
		db.insert(actor, source, param, social, dqs);

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
		nws.add(NwFactor.of(impact, 1, 1));
		db.insert(method);

		// processes
		Consumer<Process> decor = proc -> {
			var ex = proc.output(e, 1);
			ex.location = loc;
			ex.currency = eur;
			proc.location = loc;
			proc.dqSystem = dqs;
			proc.exchangeDqSystem = dqs;
			proc.socialDqSystem = dqs;
			SocialAspect.of(proc, social).source = source;

			var doc = proc.documentation = new ProcessDocumentation();
			doc.dataSetOwner = actor;
			doc.dataGenerator = actor;
			doc.dataDocumentor = actor;
			doc.publication = source;
			doc.reviewer = actor;
			doc.sources.add(source);
		};

		var pP = Process.of("pP", p);
		decor.accept(pP);
		pP.output(q, 1);
		var eex = pP.exchanges.stream()
				.filter(ex -> ex.flow.equals(e))
				.findFirst()
				.orElseThrow();
		pP.allocationFactors.addAll(List.of(
				AllocationFactor.economic(p, 1),
				AllocationFactor.economic(q, 0),
				AllocationFactor.physical(p, 1),
				AllocationFactor.physical(q, 0),
				AllocationFactor.causal(p, eex, 1),
				AllocationFactor.causal(q, eex, 0)
		));
		db.insert(pP);

		var qQ = Process.of("qQ", q);
		decor.accept(qQ);
		qQ.input(p, 1).defaultProviderId = pP.id;
		qQ.parameters.add(Parameter.process("param", 42));
		db.insert(qQ);

		// product system
		var system = ProductSystem.of("system", qQ);
		system.link(pP, qQ);
		var sysParams = new ParameterRedefSet();
		sysParams.parameters.add(
				ParameterRedef.of(qQ.parameters.get(0), qQ));
		system.parameterSets.add(sysParams);
		db.insert(system);

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
		get(DQSystem.class, "dqs");
		get(ImpactCategory.class, "impact");
		get(ImpactMethod.class, "method");
		get(Process.class, "pP");
		get(Process.class, "qQ");
		get(ProductSystem.class, "system");
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
	public void testDqs() {
		var dqs = get(DQSystem.class, "dqs");
		check(dqs.source, "source");
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

	@Test
	public void testProcesses() {
		var pP = get(Process.class, "pP");
		var qQ = get(Process.class, "qQ");
		var procs = List.of(pP, qQ);

		for (var p : procs) {

			check(p.dqSystem, "dqs");
			check(p.exchangeDqSystem, "dqs");
			check(p.socialDqSystem, "dqs");
			var doc = p.documentation;
			check(doc.dataSetOwner, "actor");
			check(doc.dataGenerator, "actor");
			check(doc.dataDocumentor, "actor");
			check(doc.publication, "source");
			check(doc.reviewer, "actor");
			check(doc.sources.get(0), "source");

			// quantitative reference
			var flow = p.name.equals("pP") ? "p" : "q";
			check(p.quantitativeReference.flow, flow);
			var qex = p.exchanges.stream()
					.filter(e -> e.flow.name.equals(flow))
					.findFirst().orElseThrow();
			assertEquals(p.quantitativeReference, qex);

			// other exchange references
			assertTrue(p.exchanges.size() > 1);
			var eex = p.exchanges.stream()
					.filter(e -> e.flow.name.equals("e"))
					.findFirst().orElseThrow();
			check(eex.location, "loc");
			check(eex.currency, "eur");
			for (var ex : p.exchanges) {
				check(ex.flowPropertyFactor.flowProperty, "mass");
				check(ex.unit, "kg");
			}
		}

		// allocation factors
		assertEquals(6, pP.allocationFactors.size());
		long p = pP.quantitativeReference.flow.id;
		long q = qQ.quantitativeReference.flow.id;
		for (var f : pP.allocationFactors) {
			var expectedProduct = f.value == 1 ? p : q;
			assertEquals(expectedProduct, f.productId);
			if (f.method == AllocationMethod.CAUSAL) {
				check(f.exchange.flow, "e");
			}
		}
	}

	@Test
	public void testSystem() {
		var sys = get(ProductSystem.class, "system");
		check(sys.referenceProcess, "qQ");
		check(sys.referenceExchange.flow, "q");
		check(sys.referenceExchange.flowPropertyFactor.flowProperty, "mass");
		check(sys.referenceExchange.unit, "kg");
		check(sys.targetUnit, "kg");
		check(sys.targetFlowPropertyFactor.flowProperty, "mass");

		var pP = get(Process.class, "pP");
		var qQ = get(Process.class, "qQ");
		assertTrue(sys.processes.containsAll(
				List.of(pP.id, qQ.id)));

		// link IDs
		var link = sys.processLinks.get(0);
		assertEquals(pP.id, link.providerId);
		assertEquals(qQ.id, link.processId);
		assertEquals(pP.quantitativeReference.flow.id, link.flowId);
		assertEquals(
				qQ.exchanges.stream()
						.filter(e -> e.flow.name.equals("p"))
						.findFirst()
						.orElseThrow()
						.id,
				link.exchangeId);

		// parameter redef.
		var param = sys.parameterSets.get(0).parameters.get(0);
		assertEquals(qQ.id, param.contextId.longValue());
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
