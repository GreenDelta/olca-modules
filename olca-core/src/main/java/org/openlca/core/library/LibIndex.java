package org.openlca.core.library;

import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.util.Strings;

/**
 * Some utility functions for writing index data.
 */
class LibIndex {

	private LibIndex() {
	}

	static Proto.ProductEntry protoEntry(int index, ProcessProduct product) {
		var entry = Proto.ProductEntry.newBuilder();
		entry.setIndex(index);
		entry.setProduct(protoFlow(product.flow));
		entry.setProcess(protoProcess(product.process));
		return entry.build();
	}

	static Proto.ElemFlowEntry protoEntry(int index, IndexFlow iFlow) {
		var entry = Proto.ElemFlowEntry.newBuilder();
		entry.setIndex(index);
		entry.setIsInput(iFlow.isInput);
		entry.setFlow(LibIndex.protoFlow(iFlow.flow));
		if (iFlow.location != null) {
			entry.setLocation(LibIndex.protoLocation(iFlow.location));
		}
		return entry.build();
	}

	static Proto.ImpactEntry protoEntry(int index, ImpactCategoryDescriptor impact) {
		var entry = Proto.ImpactEntry.newBuilder();
		entry.setIndex(index);
		entry.setImpact(protoImpact(impact));
		return entry.build();
	}

	static Proto.Process protoProcess(CategorizedDescriptor d) {
		var proto = Proto.Process.newBuilder();
		if (d == null)
			return proto.build();
		proto.setId(Strings.orEmpty(d.refId));
		proto.setName(Strings.orEmpty(d.name));
		return proto.build();
	}

	static Proto.Flow protoFlow(FlowDescriptor d) {
		var proto = Proto.Flow.newBuilder();
		if (d == null)
			return proto.build();
		proto.setId(Strings.orEmpty(d.refId));
		proto.setName(Strings.orEmpty(d.name));
		if (d.flowType != null) {
			proto.setType(d.flowType.name());
		}
		return proto.build();
	}

	static Proto.Location protoLocation(LocationDescriptor d) {
		var proto = Proto.Location.newBuilder();
		if (d == null)
			return proto.build();
		proto.setId(Strings.orEmpty(d.refId));
		proto.setName(Strings.orEmpty(d.name));
		proto.setCode(Strings.orEmpty(d.code));
		return proto.build();
	}

	static Proto.Impact protoImpact(ImpactCategoryDescriptor d) {
		var proto = Proto.Impact.newBuilder();
		if (d == null)
			return proto.build();
		proto.setId(Strings.orEmpty(d.refId));
		proto.setName(Strings.orEmpty(d.name));
		proto.setUnit(Strings.orEmpty(d.referenceUnit));
		return proto.build();
	}

}
