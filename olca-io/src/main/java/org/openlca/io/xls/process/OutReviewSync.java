package org.openlca.io.xls.process;

import org.openlca.core.model.Process;
import org.openlca.core.model.doc.Review;

class OutReviewSync {

	private final OutConfig config;
	private final Process process;

	private OutReviewSync(OutConfig config) {
		this.config = config;
		this.process = config.process();
	}

	static void sync(OutConfig config) {
		new OutReviewSync(config).sync();
	}

	private void sync() {
		if (process.documentation == null)
			return;
		var revs = process.documentation.reviews;
		if (revs.isEmpty())
			return;
		var sheet = config.createSheet(Tab.REVIEWS)
			.withColumnWidths(3, 40);
		for (var rev : revs) {
			write(rev, sheet);
		}
	}

	private void write(Review rev, SheetWriter sheet) {
		sheet.next(Section.REVIEW)
			.next(Field.REVIEW_TYPE, rev.type)
			.next(Field.REVIEW_REPORT, rev.report)
			.next(Field.REVIEW_DETAILS, rev.details)
			.next();

		if (!rev.reviewers.isEmpty()) {
			sheet.next(Section.REVIEWERS);
			for (var r : rev.reviewers) {
				sheet.next(r);
			}
			sheet.next();
		}

		if (!rev.scopes.isEmpty()) {
			sheet.next(Section.REVIEW_METHODS);
			for (var scope : rev.scopes.values()) {
				for (var method : scope.methods) {
					sheet.next(row -> row.next(scope.name).next(method));
				}
			}
			sheet.next();
		}

		if (!rev.assessment.isEmpty()) {
			sheet.next(Section.QUALITY_ASSESSMENT);
			rev.assessment.each(
				(key, val) -> sheet.next(row -> row.next(key).next(val)));
			sheet.next();
		}
	}

}
