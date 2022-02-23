package org.openlca.core.matrix.index;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Result;
import org.openlca.core.model.descriptors.RootDescriptor;
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
public record TechFlow(
        RootDescriptor provider, FlowDescriptor flow) {

	public static TechFlow of(RootDescriptor process, FlowDescriptor flow) {
		return new TechFlow(process, flow);
	}

	public static TechFlow of(Process process, Flow flow) {
		return of(Descriptor.of(process), Descriptor.of(flow));
	}

	/**
	 * Creates a product where the given product system is the provider and the
	 * reference flow of the system the product.
	 */
	public static TechFlow of(ProductSystem system) {
		Flow flow = system.referenceExchange != null
				? system.referenceExchange.flow
				: null;
		return of(Descriptor.of(system), Descriptor.of(flow));
	}

	/**
	 * Creates a process product with the quantitative reference flow of the process
	 * as the provider flow. Note that the quantitative reference flow must be a
	 * product output or waste input in this case. Make sure that this is the case
	 * and what you want when calling this method. Otherwise use another
	 * construction method.
	 */
	public static TechFlow of(Process process) {
		var flow = process.quantitativeReference != null
				? process.quantitativeReference.flow
				: null;
		return of(Descriptor.of(process), Descriptor.of(flow));
	}

	public static TechFlow of(Result result) {
		var flow = result.referenceFlow != null
			? result.referenceFlow.flow
			: null;
		return of(Descriptor.of(result), Descriptor.of(flow));
	}

	/**
	 * Returns the process of process-product pair. Note that this can be also the
	 * descriptor of a product system when it is a sub-system of another product
	 * system because in this case such systems are handled just like processes
	 * (they are mapped to the technology matrix $mathbf{A}$ via the `TechIndex`
	 * etc.).
	 */
	@Override
	public RootDescriptor provider() {
		return provider;
	}

	/**
	 * Returns the product flow of the process-product pair. Note that this can also
	 * be a waste flow (which is then an input of the process and the treatment of
	 * waste is the product of the process).
	 */
	@Override
	public FlowDescriptor flow() {
		return flow;
	}

	/**
	 * Returns true if the given process and flow ID are the same as of the process
	 * or product system and product or waste flow of this process product.
	 */
	public boolean matches(long processId, long flowId) {
		return processId == providerId() && flowId == flowId();
	}

	public long flowId() {
		return flow().id;
	}

	/**
	 * Returns the ID of the underlying process or product system of this provider.
	 */
	public long providerId() {
		return provider().id;
	}

	public LongPair pair() {
		return LongPair.of(providerId(), flowId());
	}

	public Long locationId() {
		if (provider() instanceof ProcessDescriptor)
			return ((ProcessDescriptor) provider()).location;
		return null;
	}

	/**
	 * Returns true when the flow of this "product" is (an input of) a waste flow.
	 * This means that the corresponding process is a waste treatment process in
	 * this case.
	 */
	public boolean isWaste() {
		return flow().flowType == FlowType.WASTE_FLOW;
	}

	/**
	 * Returns true if the underlying process of this product is from a library.
	 */
	public boolean isFromLibrary() {
		return provider().isFromLibrary();
	}

	/**
	 * Returns the library ID if the process of this product is from a library,
	 * otherwise `null` is returned.
	 */
	public String library() {
		return provider().library;
	}

	public boolean isProductSystem() {
		return provider.type == ModelType.PRODUCT_SYSTEM;
	}

	public boolean isProcess() {
		return provider.type == ModelType.PROCESS;
	}

	public boolean isResult() {
		return provider.type == ModelType.RESULT;
	}
}
