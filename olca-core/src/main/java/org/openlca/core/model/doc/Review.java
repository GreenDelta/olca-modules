package org.openlca.core.model.doc;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Copyable;
import org.openlca.core.model.Source;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_reviews")
public class Review extends AbstractEntity implements Copyable<Review> {

	@Column(name = "review_type")
	public String type;

	@Lob
	@Column(name = "scopes")
	@Convert(converter = ReviewScopeConverter.class)
	public final List<ReviewScope> scopes = new ArrayList<>();

	@Lob
	@Column(name = "details")
	public String details;

	@OneToMany
	@JoinTable(name = "tbl_actor_links", joinColumns = {
			@JoinColumn(name = "f_owner")}, inverseJoinColumns = {
			@JoinColumn(name = "f_actor")})
	public final List<Actor> reviewers = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "f_report")
	public Source report;

	@Override
	public Review copy() {
		var copy = new Review();
		copy.type = type;
		for (var scope : scopes) {
			copy.scopes.add(scope.copy());
		}
		copy.details = details;
		copy.reviewers.addAll(reviewers);
		copy.report = report;
		return copy;
	}
}
