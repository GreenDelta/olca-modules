package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openlca.core.model.ProcessGroup;
import org.openlca.core.model.ProcessGroupSet;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A group of processes for result analysis. If this group is tagged as rest it
 * contains all processes that are not assigned to other groups.
 */
public class ProcessGrouping {

	public String name;
	public final List<ProcessDescriptor> processes = new ArrayList<>();
	public boolean rest;

	@Override
	public int hashCode() {
		if (name != null)
			return name.hashCode();
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof ProcessGrouping))
			return false;
		ProcessGrouping other = (ProcessGrouping) obj;
		return Objects.equals(this.name, other.name);
	}

	/**
	 * Applies the given group set on the given processes. If there are
	 * processes not assignable to a group of the group set a group with these
	 * processes is created using the given parameter restName.
	 */
	public static List<ProcessGrouping> applyOn(
			Collection<ProcessDescriptor> processes, ProcessGroupSet groupSet,
			String restName) {
		if (processes == null)
			return Collections.emptyList();
		List<ProcessGroup> groups = getGroups(groupSet);
		List<ProcessGrouping> groupings = new ArrayList<>();
		List<ProcessDescriptor> rest = new ArrayList<>(processes);
		for (ProcessGroup group : groups) {
			ProcessGrouping grouping = new ProcessGrouping();
			grouping.name = group.getName();
			grouping.rest = false;
			List<ProcessDescriptor> matches = split(group.getProcessIds(),
					rest);
			grouping.processes.addAll(matches);
			groupings.add(grouping);
		}
		if (!rest.isEmpty()) {
			ProcessGrouping grouping = new ProcessGrouping();
			grouping.name = restName;
			grouping.rest = true;
			grouping.processes.addAll(rest);
			groupings.add(grouping);
		}
		return groupings;
	}

	private static List<ProcessGroup> getGroups(ProcessGroupSet groupSet) {
		try {
			if (groupSet == null)
				return Collections.emptyList();
			return groupSet.getGroups();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ProcessGrouping.class);
			log.error("Failed to read groups from set", e);
			return Collections.emptyList();
		}
	}

	private static List<ProcessDescriptor> split(List<String> processIds,
			List<ProcessDescriptor> processes) {
		List<ProcessDescriptor> matches = new ArrayList<>();
		for (String id : processIds) {
			for (ProcessDescriptor p : processes) {
				if (p.getRefId() != null && p.getRefId().equals(id))
					matches.add(p);
			}
		}
		processes.removeAll(matches);
		return matches;
	}

}
