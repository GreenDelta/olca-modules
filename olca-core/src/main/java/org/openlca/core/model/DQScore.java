package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_dq_scores")
public class DQScore extends AbstractEntity
	implements Comparable<DQScore>, Copyable<DQScore> {

	@Column(name = "position")
	public int position;

	@Column(name = "label")
	public String label;

	@Lob
	@Column(name = "description")
	public String description;

	@Column(name = "uncertainty")
	public double uncertainty;

	@Override
	public int compareTo(DQScore o) {
		if (o == null)
			return 1;
		return Integer.compare(position, o.position);
	}

	@Override
	public DQScore copy() {
		var clone = new DQScore();
		clone.position = position;
		clone.label = label;
		clone.description = description;
		clone.uncertainty = uncertainty;
		return clone;
	}

}
