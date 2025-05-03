package org.openlca.io.olca;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Process;
import org.openlca.core.model.Source;
import org.openlca.core.model.doc.ComplianceDeclaration;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.core.model.doc.Review;
import org.openlca.core.model.doc.ReviewScope;
import org.openlca.io.Tests;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class ProcessDocTest {

	private final IDatabase sourceDb = Tests.getDb();
	private final IDatabase targetDb = Derby.createInMemory();
	private Process process;
	private Actor actor;
	private Source source;

	@Before
	public void setup() {
		actor = Actor.of("Actor");
		source = Source.of("Source");
		sourceDb.insert(actor, source);
	}

	@After
	public void cleanup() {
		sourceDb.delete(actor, source);
		targetDb.clear();
		targetDb.clear();
	}

	@Test
	public void testGeneralDocFields() {
		before(doc -> {
			doc.useAdvice = "use advice";
			doc.project = "project";
		});
		after(doc -> {
			assertEquals("use advice", doc.useAdvice);
			assertEquals("project", doc.project);
		});
	}

	@Test
	public void testFlowCompleteness() {
		before(doc -> {
			var m = doc.flowCompleteness;
			m.put("Products", "All there");
			m.put("Climate change", "Some missing");
		});
		after(doc -> {
			var m = doc.flowCompleteness;
			assertEquals("All there", m.get("Products"));
			assertEquals("Some missing", m.get("Climate change"));
		});
	}

	@Test
	public void testComplianceDeclarations() {
		before(doc -> {
			for (int i = 1; i <= 3; i++) {
				var dec = new ComplianceDeclaration();
				dec.system = source;
				dec.comment = "Just a test";
				dec.aspects.put("Nomenclature", "Fully compliant");
				dec.aspects.put("Modelling", "Disaster");
				doc.complianceDeclarations.add(dec);
			}
		});
		after(doc -> {
			assertEquals(3, doc.complianceDeclarations.size());
			for (int i = 1; i <= 3; i++) {
				var dec = doc.complianceDeclarations.get(i - 1);
				assertEquals(source.refId, dec.system.refId);
				assertEquals("Just a test", dec.comment);
				assertEquals("Fully compliant", dec.aspects.get("Nomenclature"));
				assertEquals("Disaster", dec.aspects.get("Modelling"));
			}
		});
	}

	@Test
	public void testReviews() {
		before(doc -> {
			for (int i = 1; i <= 3; i++) {
				var rev = new Review();
				rev.type = "Intergalactic review panel";
				rev.details = "oh oh";
				var scope = new ReviewScope("Numbers");
				scope.methods.addAll(List.of("Cross checks", "Expert judgement"));
				rev.scopes.put(scope);
				rev.report = source;
				rev.reviewers.add(actor);
				rev.assessment.put("Data quality", "ok");
				rev.assessment.put("Modelling", "uff");
				doc.reviews.add(rev);
			}
		});
		after(doc -> {
			for (int i = 1; i <= 3; i++) {
				var rev = doc.reviews.get(i - 1);
				assertEquals("Intergalactic review panel", rev.type);
				assertEquals("oh oh", rev.details);
				var scope = rev.scopes.get("Numbers");
				assertTrue(scope.methods.contains("Cross checks"));
				assertTrue(scope.methods.contains("Expert judgement"));
				assertEquals(rev.report.refId, source.refId);
				assertEquals(rev.reviewers.getFirst().refId, actor.refId);
				assertEquals("ok", rev.assessment.get("Data quality"));
				assertEquals("uff", rev.assessment.get("Modelling"));
			}
		});
	}

	private void before(Consumer<ProcessDoc> fn) {
		process = new Process();
		process.refId = UUID.randomUUID().toString();
		process.documentation = new ProcessDoc();
		sourceDb.insert(process);
		fn.accept(process.documentation);
		process = sourceDb.update(process);
	}

	private void after(Consumer<ProcessDoc> fn) {
		new DatabaseImport(sourceDb, targetDb).run();
		sourceDb.delete(process);
		assertNull(sourceDb.get(Process.class, process.refId));
		var copy = targetDb.get(Process.class, process.refId);
		assertNotNull(copy);
		fn.accept(copy.documentation);
		targetDb.delete(copy);
	}

}
