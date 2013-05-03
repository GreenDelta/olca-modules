package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of process IDs with a name. The lower case name of the group is used
 * as identifier for the equals, hash-code, and compare-functions.
 */
public class ProcessGroup implements Comparable<ProcessGroup> {

	private String name;
	private List<String> processIds = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getProcessIds() {
		return processIds;
	}

	@Override
	public int compareTo(ProcessGroup o) {
		if (o == null)
			return 1;
		if (o.name == null && this.name == null)
			return 0;
		if (o.name == null)
			return 1;
		if (this.name == null)
			return -1;
		return this.name.compareToIgnoreCase(o.name);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof ProcessGroup))
			return false;
		return compareTo((ProcessGroup) obj) == 0;
	}

	@Override
	public int hashCode() {
		if (name != null)
			return name.toLowerCase().hashCode();
		return super.hashCode();
	}

}
