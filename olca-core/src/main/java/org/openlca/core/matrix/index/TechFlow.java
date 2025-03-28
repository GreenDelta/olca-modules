package org.openlca.core.matrix.index;

import java.util.Objects;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowResult;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.Result;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.util.Exchanges;

/// A TechFlow describes a provider-flow pair in a product system. A provider
/// is typically a process but can also be a product system or result. The flow
/// is a product output or waste input of that provider. These provider-flow
/// pairs are mapped to the rows and columns of the matrices in the inventory
/// model. Multi-output processes are split into multiple vectors that each are
/// related to a single process-flow pair.
public record TechFlow(
		RootDescriptor provider, FlowDescriptor flow
) {

	public TechFlow {
		Objects.requireNonNull(provider, "provider must not be null");
		Objects.requireNonNull(flow, "flow must not be null");
	}

	public static TechFlow of(RootDescriptor provider, FlowDescriptor flow) {
		return new TechFlow(provider, flow);
	}

	public static TechFlow of(Process process, Flow flow) {
		return of(Descriptor.of(process), Descriptor.of(flow));
	}

	/// Creates a tech-flow from the given product system with the reference flow
	/// of that system. If the system does not have a valid reference flow, an
	/// exception is thrown.
	public static TechFlow of(ProductSystem system) {
		var qRef = system.referenceExchange;
		if (!Exchanges.isProviderFlow(qRef)) {
			throw new IllegalArgumentException(
					"the reference exchange of the product system is not a provider flow");
		}
		return of(Descriptor.of(system), Descriptor.of(qRef.flow));
	}

	/// Creates a tech-flow from the given process with the quantitative reference
	/// of that process as provider flow. If the process does not have a valid
	/// quantitative reference, an exception is thrown.
	public static TechFlow of(Process process) {
		var qRef = process.quantitativeReference;
		if (!Exchanges.isProviderFlow(qRef)) {
			throw new IllegalArgumentException(
					"the quantitative reference of the process is not a provider flow");
		}
		return of(Descriptor.of(process), Descriptor.of(qRef.flow));
	}

	/// Creates a tech-flow from the given result with the reference flow of that
	/// result. If the result does not have a valid reference flow, an exception
	/// is thrown.
	public static TechFlow of(Result result) {
		var qRef = result.referenceFlow;
		if (isProviderFlow(qRef)) {
			throw new IllegalArgumentException(
					"the reference exchange of the result is not a provider flow");
		}
		return of(Descriptor.of(result), Descriptor.of(qRef.flow));
	}

	private static boolean isProviderFlow(FlowResult r) {
		if (r == null || r.flow == null || r.flow.flowType == null)
			return false;
		return switch (r.flow.flowType) {
			case PRODUCT_FLOW -> !r.isInput;
			case WASTE_FLOW -> r.isInput;
			default -> false;
		};
	}


	/// Returns `true` if the given provider and flow ID are the same as of the
	/// provider and flow of this tech-flow.
	public boolean matches(long providerId, long flowId) {
		return providerId == providerId() && flowId == flowId();
	}

	public long flowId() {
		return flow.id;
	}

	public long providerId() {
		return provider.id;
	}

	public LongPair pair() {
		return LongPair.of(providerId(), flowId());
	}

	public Long locationId() {
		return provider() instanceof ProcessDescriptor p
				? p.location
				: null;
	}

	public boolean isFromLibrary() {
		return provider().isFromLibrary();
	}

	/// Returns the library ID in case the provider is an entity from a library.
	public String library() {
		return provider().library;
	}

	/// Returns the provider type of this tech-flow.
	public ProviderType type() {
		var t = provider.type;
		if (t == null)
			return ProviderType.PROCESS;
		return switch (t) {
			case PRODUCT_SYSTEM -> ProviderType.SUB_SYSTEM;
			case RESULT -> ProviderType.RESULT;
			default -> ProviderType.PROCESS;
		};
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

	public boolean isWaste() {
		return flow.flowType == FlowType.WASTE_FLOW;
	}
}
