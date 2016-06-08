package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_dq_scores")
public class DQScore extends AbstractEntity implements Comparable<DQScore> {

	@Column(name = "position")
	public int position;

	@Lob
	@Column(name = "description")
	public String description;

	@Column(name = "uncertainty")
	public double uncertainty;

	@Override
	public int compareTo(DQScore o) {
		return Integer.compare(position, o.position);
	}

	@Override
	public DQScore clone() {
		DQScore clone = new DQScore();
		clone.position = position;
		clone.description = description;
		clone.uncertainty = uncertainty;
		return clone;
	}

}
