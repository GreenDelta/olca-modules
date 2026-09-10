package org.openlca.core.database;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.ModelReferences.ModelReference;
import org.openlca.core.model.Actor;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;
import org.openlca.core.model.TypedRefId;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.core.model.doc.Review;

public class ModelReferencesTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testDQSystemSource() {
		var source = db.insert(Source.of("source"));
		var dqSystem = DQSystem.of("dq system");
		dqSystem.source = source;
		dqSystem = db.insert(dqSystem);

		assertRefs(referencesOf(ModelType.DQ_SYSTEM, dqSystem.refId), source);
		assertRefs(usagesOf(ModelType.SOURCE, source.refId), dqSystem);

		db.delete(dqSystem, source);
	}

	@Test
	public void testReviewReviewers() {
		var actor = db.insert(Actor.of("actor"));
		var process = Process.of("process", null);
		process.documentation = new ProcessDoc();
		var review = new Review();
		review.reviewers.add(actor);
		process.documentation.reviews.add(review);
		process = db.insert(process);

		assertRefs(referencesOf(ModelType.PROCESS, process.refId), actor);
		assertRefs(usagesOf(ModelType.ACTOR, actor.refId), process);

		db.delete(process, actor);
	}

	@Test
	public void testProductSystemProcesses() {
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);
		var q = Flow.product("q", mass);
		var P = Process.of("P", p);
		var Q = Process.of("Q", q);
		db.insert(units, mass, p, q, P, Q);

		// Q is contained in the system but it is neither the reference
		// process nor connected through a process link
		var system = ProductSystem.of("S", P);
		system.processes.add(Q.id);
		system = db.insert(system);

		assertRefs(referencesOf(ModelType.PRODUCT_SYSTEM, system.refId), P, Q);
		assertRefs(usagesOf(ModelType.PROCESS, Q.refId), system);

		db.delete(system, P, Q, p, q, mass, units);
	}

	private List<ModelReference> referencesOf(ModelType type, String refId) {
		var refs = new ArrayList<ModelReference>();
		ModelReferences.scan(db).iterateReferences(
			new TypedRefId(type, refId), ref -> {
				refs.add(ref);
			});
		return refs;
	}

	private List<ModelReference> usagesOf(ModelType type, String refId) {
		var refs = new ArrayList<ModelReference>();
		ModelReferences.scan(db).iterateUsages(
			new TypedRefId(type, refId), ref -> {
				refs.add(ref);
			});
		return refs;
	}

	private void assertRefs(List<ModelReference> refs, RootEntity... expected) {
		assertEquals(expected.length, refs.size());
		for (var ref : refs) {
			var found = false;
			for (var e : expected) {
				if (ModelType.of(e) == ref.type
					&& e.refId.equals(ref.refId)
					&& e.id == ref.id) {
					found = true;
					break;
				}
			}
			assertTrue("unexpected reference " + ref, found);
		}
	}
}
