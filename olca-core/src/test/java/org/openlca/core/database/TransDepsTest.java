package org.openlca.core.database;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.core.model.doc.Review;

public class TransDepsTest {

	private final IDatabase db = Tests.getDb();

	private Source source;
	private Actor actor;

	@Before
	public void setup() {
		source = db.insert(Source.of("some source"));
		actor = db.insert(Actor.of("some actor"));
	}

	@After
	public void cleanup() {
		db.delete(
				source,
				actor
		);
	}


	@Test
	public void testProcessDeps() {

		// reviews
		var revProc = new Process();
		revProc.documentation = new ProcessDoc();
		var rev = new Review();
		revProc.documentation.reviews.add(rev);
		rev.report = source;
		rev.reviewers.add(actor);
		revProc = db.insert(revProc);
		var deps = TransDeps.of(revProc, db);
		assertContains(deps, revProc, source, actor);
		db.delete(revProc);


	}

	private void assertContains(List<RootDescriptor> deps, RootEntity... es) {
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
