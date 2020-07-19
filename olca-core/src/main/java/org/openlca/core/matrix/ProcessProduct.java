package org.openlca.core.matrix;

import java.util.Objects;
import java.util.Optional;

import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * In openLCA, we map the process-product pairs of a product system to the
 * respective rows and columns of the matrices in the inventory model.
 * Multi-output processes are split into multiple vectors that each are relate
 * to a single process-product pair. This also includes waste treatment
 * processes (where the treatment of waste is the product) and product systems
 * that are sub-systems of other product systems (and are handled like processes
 * in these systems with their quantitative reference as product).
 */
public class ProcessProduct {

	/**
	 * The process of process-product pair. Note that this can be also the
	 * descriptor of a product system when it is a sub-system of another product
	 * system because in this case such systems are handled just like processes
	 * (they are mapped to the technology matrix $mathbf{A}$ via the `TechIndex`
	 * etc.).
	 */
	public CategorizedDescriptor process;

	/**
	 * The product flow of the process-product pair. Note that this can also be
	 * a waste flow (which is then an input of the process and the treatment of
	 * waste is the product of the process).
	 */
	public FlowDescriptor flow;

	public static ProcessProduct of(
			CategorizedDescriptor entity,
			FlowDescriptor flow) {
		var p = new ProcessProduct();
		p.process = entity;
		p.flow = flow;
		return p;
	}

	public static ProcessProduct of(
			Process process,
			Flow flow) {
		return of(Descriptor.of(process),
				Descriptor.of(flow));
	}

	/**
	 * Creates a product where the given product system is the provider and the
	 * reference flow of the system the product.
	 */
	public static ProcessProduct of(ProductSystem system) {
		Flow flow = system.referenceExchange != null
				? system.referenceExchange.flow
				: null;
		return of(Descriptor.of(system),
				Descriptor.of(flow));
	}

	/**
	 * Creates a process product with the quantitative reference flow of the
	 * process as the provider flow. Note that the quantitative reference flow
	 * must be a product output or waste input in this case. Make sure that this
	 * is the case and what you want when calling this method. Otherwise use
	 * another construction method.
	 */
	public static ProcessProduct of(Process process) {
		var flow = process.quantitativeReference != null
				? process.quantitativeReference.flow
				: null;
		return of(Descriptor.of(process),
				Descriptor.of(flow));
	}

	@Override
	public int hashCode() {
		return Objects.hash(process, flow);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ProcessProduct))
			return false;
		var other = (ProcessProduct) obj;
		return Objects.equals(this.process, other.process)
				&& Objects.equals(this.flow, other.flow);
	}

	public boolean equals(long id, long flowId) {
		return id == id() && flowId == flowId();
	}

	public long flowId() {
		return flow == null ? 0L : flow.id;
	}

	/**
	 * Returns the ID of the underlying process or product system of this
	 * provider.
	 */
	public long id() {
		return process == null ? 0L : process.id;
	}

	public LongPair pair() {
		return LongPair.of(id(), flowId());
	}

	public Long locationId() {
		if (process instanceof ProcessDescriptor)
			return ((ProcessDescriptor) process).location;
		return null;
	}

	public Optional<String> getLibrary() {
		return process == null || process.library == null
				? Optional.empty()
				: Optional.of(process.library);
	}
}
