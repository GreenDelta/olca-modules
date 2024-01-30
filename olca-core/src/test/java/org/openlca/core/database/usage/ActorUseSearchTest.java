package org.openlca.core.database.usage;


import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Process;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.core.model.doc.Review;

public class ActorUseSearchTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testFindNoUsage() {
		var actor = db.insert(Actor.of("actor"));
		UsageTests.expectEmpty(actor);
		db.delete(actor);
	}

	@Test
	public void testFindInProcessDocs() {
		var actor = db.insert(Actor.of("actor"));
		var process = createProcessWithDoc(actor);
		UsageTests.expectOne(actor, process);
		db.delete(process, actor);
	}

	@Test
	public void testFindInReview() {
		var actor = db.insert(Actor.of("actor"));
		var process = createProcessWithReviewedDoc(actor);
		UsageTests.expectOne(actor, process);
		db.delete(process, actor);
	}

	@Test
	public void testFindInEpd() {
		var actor = db.insert(Actor.of("actor"));
		var process = createEpd(actor);
		UsageTests.expectOne(actor, process);
		db.delete(process, actor);
	}


	private Process createProcessWithDoc(Actor actor) {
		var documentation = new ProcessDoc();
		var process = new Process();
		process.name = "process";
		process.documentation = documentation;
		process.documentation.dataOwner = actor;
		process.documentation.dataGenerator = actor;
		process.documentation.dataDocumentor = actor;
		return db.insert(process);
	}

	private Process createProcessWithReviewedDoc(Actor actor) {
		var review = new Review();
		review.reviewers.add(actor);
		var documentation = new ProcessDoc();
		var process = new Process();
		process.name = "process";
		process.documentation = documentation;
		process.documentation.reviews.add(review);
		return db.insert(process);
	}

	private Epd createEpd(Actor actor) {
		var epd = new Epd();
		epd.manufacturer = actor;
		epd. verifier = actor;
		epd.programOperator = actor;
		return db.insert(epd);
	}

}
