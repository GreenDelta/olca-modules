package org.openlca.proto.io.output;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.proto.ProtoExchangeRef;
import org.openlca.proto.ProtoParameterRedef;
import org.openlca.proto.ProtoParameterRedefSet;
import org.openlca.proto.ProtoProcessLink;
import org.openlca.proto.ProtoProductSystem;
import org.openlca.proto.ProtoType;
import org.openlca.util.Strings;

public class ProductSystemWriter {

	private final WriterConfig config;

	public ProductSystemWriter(WriterConfig config) {
		this.config = config;
	}

	public ProtoProductSystem write(ProductSystem system) {
		var proto = ProtoProductSystem.newBuilder();
		if (system == null)
			return proto.build();
		proto.setType(ProtoType.ProductSystem);
		Out.map(system, proto);
		mapQRef(system, proto);
		var processes = mapProcesses(system, proto);
		mapLinks(system, proto, processes);
		mapParameterSets(system, proto);
		return proto.build();
	}

	private void mapQRef(ProductSystem system,
		ProtoProductSystem.Builder proto) {
		// ref. process
		config.dep(system.referenceProcess, proto::setRefProcess);

		// ref. exchange
		if (system.referenceExchange != null) {
			var e = ProtoExchangeRef.newBuilder()
				.setInternalId(system.referenceExchange.internalId);
			proto.setRefExchange(e);
		}

		// ref. quantity
		if (system.targetFlowPropertyFactor != null) {
			config.dep(
				system.targetFlowPropertyFactor.flowProperty,
				proto::setTargetFlowProperty);
		}

		config.dep(system.targetUnit, proto::setTargetUnit);
		proto.setTargetAmount(system.targetAmount);
	}

	private TLongObjectHashMap<RootDescriptor> mapProcesses(
		ProductSystem system, ProtoProductSystem.Builder proto) {
		var types = new ModelType[]{
			ModelType.PROCESS, ModelType.PRODUCT_SYSTEM, ModelType.RESULT,
		};
		var resolved = new TLongObjectHashMap<RootDescriptor>();
		for (var id : system.processes) {
			RootDescriptor d = null;
			for (var type : types) {
				d = config.getDescriptor(type, id);
				if (d != null)
					break;
			}
			if (d == null)
				continue;
			resolved.put(d.id, d);
			config.dep(d, proto::addProcesses);
		}
		return resolved;
	}

	private void mapLinks(ProductSystem system, ProtoProductSystem.Builder proto,
		TLongObjectHashMap<RootDescriptor> processes) {

		// map the exchange IDs to process internal IDs
		var usedExchanges = new TLongHashSet();
		for (var link : system.processLinks) {
			usedExchanges.add(link.exchangeId);
		}
		var exchangeIds = config.mapExchangeIdsOf(usedExchanges);

		// add the links
		for (var link : system.processLinks) {
			var protoLink = ProtoProcessLink.newBuilder();

			// provider
			var provider = processes.get(link.providerId);
			if (provider != null) {
				protoLink.setProvider(Refs.refOf(provider));
			}

			// process
			var process = processes.get(link.processId);
			if (process != null) {
				protoLink.setProcess(Refs.refOf(process));
			}

			// flow
			var flow = config.getDescriptor(ModelType.FLOW, link.flowId);
			if (flow != null) {
				protoLink.setFlow(Refs.refOf(flow));
			}

			// linked exchange
			var eid = exchangeIds.get(link.exchangeId);
			if (eid != 0) {
				protoLink.setExchange(
					ProtoExchangeRef.newBuilder()
						.setInternalId(eid)
						.build());
			}

			// add the link
			proto.addProcessLinks(protoLink);
		}
	}

	private void mapParameterSets(ProductSystem system,
		ProtoProductSystem.Builder proto) {
		for (var paramSet : system.parameterSets) {
			var protoSet = ProtoParameterRedefSet.newBuilder();
			protoSet.setName(Strings.notNull(paramSet.name));
			protoSet.setDescription(Strings.notNull(paramSet.description));
			protoSet.setIsBaseline(paramSet.isBaseline);
			for (var redef : paramSet.parameters) {
				var protoRedef = ProtoParameterRedef.newBuilder();
				protoRedef.setName(Strings.notNull(redef.name));
				protoRedef.setValue(redef.value);
				if (redef.uncertainty != null) {
					var u = Out.uncertaintyOf(redef.uncertainty);
					protoRedef.setUncertainty(u);
				}
				if (redef.contextId != null) {
					var context = config.getDescriptor(
						redef.contextType, redef.contextId);
					config.dep(context, protoRedef::setContext);
				}
				protoSet.addParameters(protoRedef);
			}
			proto.addParameterSets(protoSet);
		}
	}
}
