package org.openlca.core.model;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.doc.Review;
import org.openlca.core.model.doc.ReviewScope;

import static org.junit.Assert.assertEquals;

public class ReviewTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testReadWrite() {

		var a = Actor.of("A");
		var s = Source.of("S");
		var p = new Process();
		p.documentation = new ProcessDoc();
		var r = new Review();
		r.reviewers.add(a);
		r.report = s;
		r.type = "Internal review";
		var scope = new ReviewScope("Raw data");
		scope.methods.add("Simple math checks");
		r.scopes.add(scope);
		r.details = "all fine";
		p.documentation.reviews.add(r);
		db.insert(a, s, p);

		p = db.get(Process.class, p.id);
		r = p.documentation.reviews.get(0);
		assertEquals(a.refId, r.reviewers.get(0).refId);
		assertEquals(s.refId, r.report.refId);
		assertEquals("Internal review", r.type);
		scope = r.scopes.get(0);
		assertEquals("Raw data", scope.name);
		assertEquals("Simple math checks", scope.methods.get(0));
		assertEquals("all fine", r.details);
		db.delete(p, a, s);
	}

}
