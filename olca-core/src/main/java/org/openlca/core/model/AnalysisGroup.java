package org.openlca.core.model;

import java.util.HashSet;
import java.util.Set;

import org.openlca.commons.Copyable;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_analysis_groups")
public class AnalysisGroup extends AbstractEntity
		implements Copyable<AnalysisGroup> {

	@Column(name = "name")
	public String name;

	@Column(name = "color")
	public String color;

	@ElementCollection
	@Column(name = "f_process")
	@CollectionTable(
			name = "tbl_analysis_group_processes",
			joinColumns = {@JoinColumn(name = "f_analysis_group")})
	public final Set<Long> processes = new HashSet<>();

	@Override
	public AnalysisGroup copy() {
		var copy = new AnalysisGroup();
		copy.name = name;
		copy.color = color;
		copy.processes.addAll(processes);
		return copy;
	}
}
