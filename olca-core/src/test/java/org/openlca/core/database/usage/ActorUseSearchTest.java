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
	public void testNoUsage() {
		var actor = db.insert(Actor.of("actor"));
		UsageTests.expectEmpty(actor);
		db.delete(actor);
	}

	@Test
	public void testFindInProcessDocs() {
		var actor = db.insert(Actor.of("actor"));
		for (int i = 0; i < 4; i++) {
			var p = new Process();
			p.name = "process";
			p.documentation = new ProcessDoc();
			switch (i) {
				case 0 -> p.documentation.dataOwner = actor;
				case 1 -> p.documentation.dataGenerator = actor;
				case 2 -> p.documentation.dataDocumentor = actor;
				default -> {
					var review = new Review();
					review.reviewers.add(actor);
					p.documentation.reviews.add(review);
				}
			}
			db.insert(p);
			UsageTests.expectOne(actor, p);
			db.delete(p);
			UsageTests.expectEmpty(actor);
		}
		db.delete(actor);
	}

	@Test
	public void testFindInEpd() {
		var actor = db.insert(Actor.of("actor"));
		for (int i = 0; i < 3; i++) {
			var epd = new Epd();
			switch (i) {
				case 0 -> epd.manufacturer = actor;
				case 1 -> epd.verifier = actor;
				default -> epd.programOperator = actor;
			}
			db.insert(epd);
			UsageTests.expectOne(actor, epd);
			db.delete(epd);
			UsageTests.expectEmpty(actor);
		}
		db.delete(actor);
	}
}
