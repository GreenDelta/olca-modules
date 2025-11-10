package org.openlca.io.xls.process;

import java.util.List;

import org.openlca.commons.Strings;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Source;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.core.model.doc.Review;

class InReviewSync {
	private final InConfig config;
	private final List<Review> reviews;

	private InReviewSync(InConfig config) {
		this.config = config;
		var process = config.process();
		if (process.documentation == null) {
			process.documentation = new ProcessDoc();
		}
		reviews = process.documentation.reviews;
	}

	static void sync(InConfig config) {
		new InReviewSync(config).sync();
	}

	private void sync() {
		var sheet = config.getSheet(Tab.REVIEWS);
		if (sheet == null)
			return;
		Review review = null;
		var it = sheet.sheetObject().rowIterator();
		while (it.hasNext()) {

			var row = it.next();
			var head = In.stringOf(row, 0);

			// match review section
			if (Section.REVIEW.matches(head)) {
				review = new Review();
				reviews.add(review);
				continue;
			}
			if (review == null)
				continue;

			// match review fields
			if (Field.REVIEW_TYPE.matches(head)) {
				review.type = In.stringOf(row, 1);
				continue;
			}
			if (Field.REVIEW_REPORT.matches(head)) {
				review.report = config.index().get(
					Source.class, In.stringOf(row, 1));
				continue;
			}
			if (Field.REVIEW_DETAILS.matches(head)) {
				review.details = In.stringOf(row, 1);
				continue;
			}

			// reviewers
			if (Section.REVIEWERS.matches(head)) {
				for (var iRow : sheet.eachBlockRowAfter(row)) {
					var name = In.stringOf(iRow, 0);
					if (Strings.isBlank(name))
						break;
					var reviewer = config.index().get(
						Actor.class, name);
					if (reviewer != null) {
						review.reviewers.add(reviewer);
					}
				}
				continue;
			}

			// scopes
			if (Section.REVIEW_METHODS.matches(head)) {
				for (var iRow : sheet.eachBlockRowAfter(row)) {
					var name = In.stringOf(iRow, 0);
					if (Strings.isBlank(name))
						break;
					var method = In.stringOf(iRow, 1);
					if (Strings.isNotBlank(method)) {
						var scope = review.scopes.createIfAbsent(name);
						scope.methods.add(method);
					}
				}
				continue;
			}

			// quality assessment
			if (Section.QUALITY_ASSESSMENT.matches(head)) {
				for (var iRow : sheet.eachBlockRowAfter(row)) {
					var aspect = In.stringOf(iRow, 0);
					if (Strings.isBlank(aspect))
						break;
					var value = In.stringOf(iRow, 1);
					if (Strings.isNotBlank(value)) {
						review.assessment.put(aspect, value);
					}
				}
			}
		}
	}
}
