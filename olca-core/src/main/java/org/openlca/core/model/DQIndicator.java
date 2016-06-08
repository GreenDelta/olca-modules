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
	public List<DQScore> scores = new ArrayList<>();

	@Override
	public int compareTo(DQIndicator o) {
		return Integer.compare(position, o.position);
	}
	
}
