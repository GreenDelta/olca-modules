package org.openlca.core.matrix;

import java.util.Objects;

import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.FlowDescriptor;

/**
 * A provider describes a process or product system which provides a link to one
 * or more processes in a product system. Thus, it describes the provider part
 * of a process link and is mapped to the index of the technology matrix.
 */
public class Provider {

	public CategorizedDescriptor entity;
	public FlowDescriptor flow;

	public static Provider of(
			CategorizedDescriptor entity,
			FlowDescriptor flow) {
		Provider p = new Provider();
		p.entity = entity;
		p.flow = flow;
		return p;
	}

	public static Provider of(
			Process process,
			Flow flow) {
		return of(Descriptors.toDescriptor(process),
				Descriptors.toDescriptor(flow));
	}

	@Override
	public int hashCode() {
		return Objects.hash(entity, flow);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Provider))
			return false;
		Provider other = (Provider) obj;
		return Objects.equals(this.entity, other.entity)
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
		return entity == null ? 0L : entity.getId();
	}

	public LongPair pair() {
		return LongPair.of(id(), flowId());
	}

}
