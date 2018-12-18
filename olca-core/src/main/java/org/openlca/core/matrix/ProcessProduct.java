package org.openlca.core.matrix;

import java.util.Objects;

import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * A provider describes a process or product system which provides a link to one
 * or more processes in a product system. Thus, it describes the provider part
 * of a process link and is mapped to the index of the technology matrix.
 */
public class ProcessProduct {

	public CategorizedDescriptor process;
	public FlowDescriptor flow;

	public static ProcessProduct of(
			CategorizedDescriptor entity,
			FlowDescriptor flow) {
		ProcessProduct p = new ProcessProduct();
		p.process = entity;
		p.flow = flow;
		return p;
	}

	public static ProcessProduct of(
			Process process,
			Flow flow) {
		return of(Descriptors.toDescriptor(process),
				Descriptors.toDescriptor(flow));
	}

	@Override
	public int hashCode() {
		return Objects.hash(process, flow);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ProcessProduct))
			return false;
		ProcessProduct other = (ProcessProduct) obj;
		return Objects.equals(this.process, other.process)
				&& Objects.equals(this.flow, other.flow);
	}

	boolean equals(long id, long flowId) {
		return id == id() && flowId == flowId();
	}

	public long flowId() {
		return flow == null ? 0L : flow.getId();
	}

	/**
	 * Returns the ID of the underlying process or product system of this
	 * provider.
	 */
	public long id() {
		return process == null ? 0L : process.getId();
	}

	public LongPair pair() {
		return LongPair.of(id(), flowId());
	}

	public Long locationId() {
		if (process instanceof ProcessDescriptor)
			return ((ProcessDescriptor) process).getLocation();
		return null;
	}

}
