package org.openlca.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.doc.AspectMap;
import org.openlca.core.model.doc.ComplianceDeclaration;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.core.model.doc.Review;
import org.openlca.core.model.doc.ReviewScope;
import org.openlca.core.model.doc.ReviewScopeMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

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
		r.scopes.put(scope);
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
		scope = r.scopes.get("Raw data");
		assertEquals("Raw data", scope.name);
		assertTrue(scope.methods.contains("Simple math checks"));
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
		dec.comment = "some details";
		dec.aspects.put("Nomenclature", "Fully compliant");
		dec.aspects.put("Documentation", "Not compliant");
		doc.complianceDeclarations.add(dec);

		db.insert(system, p);
		p = db.get(Process.class, p.id);
		dec = p.documentation.complianceDeclarations.get(0);
		assertEquals(dec.system, system);
		assertEquals("some details", dec.comment);
		assertEquals("Fully compliant", dec.aspects.get("Nomenclature"));
		assertEquals("Not compliant", dec.aspects.get("Documentation"));

		db.delete(p, system);
	}

	@Test
	public void testFlowCompleteness() {
		var p = new Process();
		db.insert(p);
		var doc = p.documentation = new ProcessDoc();
		doc.flowCompleteness.put("Product flows", "All flows");
		doc.flowCompleteness.put("Climate change", "Flows missing");
		db.update(p);
		db.clearCache();

		p = db.get(Process.class, p.id);
		var c = p.documentation.flowCompleteness;
		assertEquals("All flows", c.get("Product flows"));
		assertEquals("Flows missing", c.get("Climate change"));
		db.delete(p);
	}

	@Test
	public void testAspectSerialization() {

		var p = new Process();
		var doc = p.documentation = new ProcessDoc();
		db.insert(p);
		doc.flowCompleteness.put("Product flows", "Flows missing");
		db.update(p);

		var ref = new AtomicReference<String>();
		var q = "select flow_completeness from tbl_process_docs " +
				"where id = " + doc.id;
		NativeSql.on(db).query(q, r -> {
			ref.set(r.getString(1));
			return false;
		});

		var json = new Gson().fromJson(ref.get(), JsonArray.class);
		var aspects = AspectMap.fromJson(json);
		assertEquals("Flows missing", aspects.get("Product flows"));
		assertNull(aspects.get("Climate change"));

		db.delete(p);
	}

	@Test
	public void testReviewScopeSerialization() {
		var p = new Process();
		var doc = p.documentation = new ProcessDoc();
		doc.reviews.add(new Review());
		db.insert(p);

		var rev = doc.reviews.get(0);
		var scope = new ReviewScope("Documentation");
		scope.methods.add("Reading");
		rev.scopes.put(scope);
		db.update(p);

		var q = "select scopes from tbl_reviews where id = " + rev.id;
		var ref = new AtomicReference<String>();
		NativeSql.on(db).query(q, r -> {
			ref.set(r.getString(1));
			return false;
		});

		var json = new Gson().fromJson(ref.get(), JsonArray.class);
		var scopes = ReviewScopeMap.fromJson(json);
		assertEquals(1, scopes.size());
		var s = scopes.get("Documentation");
		assertEquals("Documentation", s.name);
		assertEquals(1, s.methods.size());
		assertTrue(s.methods.contains("Reading"));

		db.delete(p);
	}

	@Test
	public void testReviewAspectSerialization() {
		var p = new Process();
		var doc = p.documentation = new ProcessDoc();
		doc.reviews.add(new Review());
		db.insert(p);

		var rev = doc.reviews.get(0);
		rev.assessment
				.put("Data quality", "Very good")
				.put("Modelling", "Disastrous");
		db.update(p);

		var q = "select assessment from tbl_reviews where id = " + rev.id;
		var ref = new AtomicReference<String>();
		NativeSql.on(db).query(q, r -> {
			ref.set(r.getString(1));
			return false;
		});

		var json = new Gson().fromJson(ref.get(), JsonArray.class);
		var aspects = AspectMap.fromJson(json);
		assertEquals("Very good", aspects.get("Data quality"));
		assertEquals("Disastrous", aspects.get("Modelling"));
	}
}
