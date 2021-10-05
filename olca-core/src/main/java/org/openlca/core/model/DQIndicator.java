package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_dq_indicators")
public class DQIndicator extends AbstractEntity
	implements Comparable<DQIndicator>, Copyable<DQIndicator> {

	@Column(name = "name")
	public String name;

	@Column(name = "position")
	public int position;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "f_dq_indicator")
	public final List<DQScore> scores = new ArrayList<>();

	@Override
	public int compareTo(DQIndicator o) {
		if (o == null)
			return 1;
		return Integer.compare(position, o.position);
	}

	@Override
	public DQIndicator copy() {
		var clone = new DQIndicator();
		clone.name = name;
		clone.position = position;
		for (var score : scores) {
			clone.scores.add(score.copy());
		}
		return clone;
	}

	public DQScore getScore(int pos) {
		for (var score : scores) {
			if (score.position == pos)
				return score;
		}
		return null;
	}

}
