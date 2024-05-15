package org.openlca.core.database;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.doc.ComplianceDeclaration;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.core.model.doc.Review;

public class TransDepsTest {

	private final IDatabase db = Tests.getDb();

	private Source source;
	private Actor actor;
	private Location location;
	private DQSystem dqSystem;
	private UnitGroup units;
	private FlowProperty flowProp;
	private Flow flow;

	@Before
	public void setup() {
		source = db.insert(Source.of("some source"));
		actor = db.insert(Actor.of("some actor"));
		location = db.insert(Location.of("Global", "GLO"));
		dqSystem = db.insert(DQSystem.of("DQ System"));
		units = db.insert(UnitGroup.of("Units of mass", "kg"));
		flowProp = db.insert(FlowProperty.of("Mass", units));
		flow = db.insert(Flow.elementary("CO2", flowProp));
	}

	@After
	public void cleanup() {
		db.delete(
				flow,
				flowProp,
				units,
				location,
				dqSystem,
				source,
				actor
		);
	}

	@Test
	public void testOnlySelfRef() {
		var es = List.of(
				new Project(),
				new ImpactMethod(),
				new ImpactCategory(),
				new ProductSystem(),
				new Process(),
				new Flow(),
				new FlowProperty(),
				new UnitGroup(),
				new Actor(),
				new Source(),
				new Location(),
				new SocialIndicator(),
				new Currency(),
				new Parameter(),
				new DQSystem(),
				new Result(),
				new Epd()
		);
		for (var e : es) {
			assertTrue(TransDeps.of(e, db).isEmpty());
			db.insert(e);
			var deps = TransDeps.of(e, db);
			db.delete(e);
			assertDeps(deps, e);
		}
	}

	@Test
	public void testParameters() {
		var globalA = Parameter.global("A", "2 * B");
		var globalB = Parameter.global("B", 42);
		db.insert(globalA, globalB);

		assertDeps(TransDeps.of(globalA, db), globalA, globalB);

		// process parameters & formulas
		var proc = new Process();
		var procA = Parameter.process("A", 21);
		proc.parameters.add(procA);
		proc.output(flow, 21).formula = "A + B";
		db.insert(proc);
		// it must only find global B, because A is a local parameter
		assertDeps(
				TransDeps.of(proc, db), proc, flow, flowProp, units, globalB);
		db.delete(proc);

		// impact factors
		var imp = ImpactCategory.of("I");
		imp.factor(flow, 21).formula = "2 * A";
		db.insert(imp);
		assertDeps(
				TransDeps.of(imp, db), imp, flow, flowProp, units, globalA, globalB);
		db.delete(imp);

		// projects
		var project = new Project();
		var variant = new ProjectVariant();
		project.variants.add(variant);
		variant.parameterRedefs.add(ParameterRedef.of(globalA));
		db.insert(project);
		assertDeps(TransDeps.of(project, db), project, globalA, globalB);
		db.delete(project);

		// product systems
		var system = new ProductSystem();
		var redefSet = new ParameterRedefSet();
		system.parameterSets.add(redefSet);
		redefSet.parameters.add(ParameterRedef.of(globalA));
		db.insert(system);
		assertDeps(TransDeps.of(system, db), system, globalA, globalB);
		db.delete(system);

		db.delete(globalA, globalB);
	}

	@Test
	public void testProcessDeps() {

		// general dependencies
		var proc = new Process();
		proc.output(flow, 1.0);
		proc.location = location;
		proc.dqSystem = dqSystem;
		db.insert(proc);
		var procDeps = TransDeps.of(proc, db);
		db.delete(proc);
		assertDeps(procDeps, proc, location, dqSystem, flow, flowProp, units);

		// doc dependencies
		var docProc = new Process();
		var doc = docProc.documentation = new ProcessDoc();
		doc.dataGenerator = actor;
		doc.sources.add(source);
		db.insert(docProc);
		var docDeps = TransDeps.of(docProc, db);
		db.delete(docProc);
		assertDeps(docDeps, docProc, actor, source);

		// reviews
		var revProc = new Process();
		revProc.documentation = new ProcessDoc();
		var rev = new Review();
		revProc.documentation.reviews.add(rev);
		rev.report = source;
		rev.reviewers.add(actor);
		revProc = db.insert(revProc);
		var revDeps = TransDeps.of(revProc, db);
		assertDeps(revDeps, revProc, source, actor);
		db.delete(revProc);

		// compliance systems
		var compProc = new Process();
		var dec = new ComplianceDeclaration();
		dec.system = source;
		compProc.documentation = new ProcessDoc();
		compProc.documentation.complianceDeclarations.add(dec);
		db.insert(compProc);
		var compDeps = TransDeps.of(compProc, db);
		db.delete(compProc);
		assertDeps(compDeps, compProc, source);
	}

	private void assertDeps(List<RootDescriptor> deps, RootEntity... es) {
		assertEquals("did not find all dependencies", es.length, deps.size());
		for (var e : es) {
			boolean found = false;
			var type = ModelType.of(e);
			for (var dep : deps) {
				if (dep.type == type && dep.id == e.id) {
					found = true;
					break;
				}
			}
			assertTrue("could not find entity in dependencies: " + e, found);
		}
	}

}
