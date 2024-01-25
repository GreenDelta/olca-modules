package org.openlca.core.model;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.doc.ComplianceDeclaration;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.core.model.doc.Review;
import org.openlca.core.model.doc.ReviewScope;

import static org.junit.Assert.assertEquals;

public class ProcessDocTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testReviews() {

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
		r.assessment.put("Documentation", "Very good");
		r.assessment.put("Model", "OK");
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
		assertEquals("Very good", r.assessment.get("Documentation"));
		assertEquals("OK", r.assessment.get("Model"));
		db.delete(p, a, s);
	}

	@Test
	public void testComplianceDeclarations() {
		var system = Source.of("Compliance system");
		var p = new Process();
		var doc = p.documentation = new ProcessDoc();
		var dec = new ComplianceDeclaration();
		dec.system = system;
		dec.details = "some details";
		dec.aspects.put("Nomenclature", "Fully compliant");
		dec.aspects.put("Documentation", "Not compliant");
		doc.complianceDeclarations.add(dec);

		db.insert(system, p);
		p = db.get(Process.class, p.id);
		dec = p.documentation.complianceDeclarations.get(0);
		assertEquals(dec.system , system);
		assertEquals("some details", dec.details);
		assertEquals("Fully compliant", dec.aspects.get("Nomenclature"));
		assertEquals("Not compliant", dec.aspects.get("Documentation"));

		db.delete(p, system);
	}

	@Test
	public void testFlowCompleteness() {
		var p = new Process();
		var doc = p.documentation = new ProcessDoc();
		doc.flowCompleteness.put("Product flows", "All flows");
		doc.flowCompleteness.put("Climate change", "Flows missing");

		db.insert(p);
		p = db.get(Process.class, p.id);
		var c = p.documentation.flowCompleteness;
		assertEquals("All flows", c.get("Product flows"));
		assertEquals("Flows missing", c.get("Climate change"));
	}

}
