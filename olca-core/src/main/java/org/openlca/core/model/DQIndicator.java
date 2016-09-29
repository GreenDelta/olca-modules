package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_dq_indicators")
public class DQIndicator extends AbstractEntity implements Comparable<DQIndicator> {

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
	public DQIndicator clone() {
		DQIndicator clone = new DQIndicator();
		clone.name = name;
		clone.position = position;
		for (DQScore score : scores) {
			clone.scores.add(score.clone());
		}
		return clone;
	}

	public DQScore getScore(int pos) {
		for (DQScore score : scores) {
			if (score.position == pos)
				return score;
		}
		return null;
	}

}
