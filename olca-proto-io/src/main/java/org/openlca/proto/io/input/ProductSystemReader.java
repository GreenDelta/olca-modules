
package org.openlca.proto.io.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.NativeSql;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.Result;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.proto.ProtoProductSystem;
import org.openlca.proto.ProtoRef;

public class ProductSystemReader
	implements EntityReader<ProductSystem, ProtoProductSystem> {

	private final EntityResolver resolver;
	private final Map<String, Descriptor> processes = new HashMap<>();

	public ProductSystemReader(EntityResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public ProductSystem read(ProtoProductSystem proto) {
		var system = new ProductSystem();
		update(system, proto);
		return system;
	}

	@Override
	public void update(ProductSystem system, ProtoProductSystem proto) {

		// clear resources in case the reader is re-used
		processes.clear();

		Util.mapBase(system, ProtoBox.of(proto), resolver);
		mapQRef(system, proto);
		addParameterSets(system, proto);

		// add processes and cache them
		system.processes.clear();
		for (int i = 0; i < proto.getProcessesCount(); i++) {
			var ref = proto.getProcesses(i);
			addProcess(system, ref);
		}

		// add link references and cache them
		system.processLinks.clear();
		var linkRefs = new ArrayList<LinkRef>();
		for (int i = 0; i < proto.getProcessLinksCount(); i++) {
			var protoLink = proto.getProcessLinks(i);
			var provider = addProcess(system, protoLink.getProvider());
			var process = addProcess(system, protoLink.getProcess());
			var flow = resolver.getDescriptor(Flow.class, protoLink.getFlow().getId());
			var exchangeId = protoLink.getExchange().getInternalId();
			if (provider == null
				|| process == null
				|| flow == null)
				continue;
			linkRefs.add(new LinkRef(provider, process, flow, exchangeId));
		}

		resolveLinks(system, linkRefs);
	}

	private void mapQRef(ProductSystem system, ProtoProductSystem proto) {
		system.targetAmount = proto.getTargetAmount();
		system.referenceProcess = Util.getProcess(resolver, proto.getRefProcess());

		Runnable clearQRef = () -> {
			system.referenceExchange = null;
			system.targetFlowPropertyFactor = null;
			system.targetUnit = null;
		};

		if (system.referenceProcess == null) {
			clearQRef.run();
			return;
		}

		var exchangeId = proto.getRefExchange().getInternalId();
		var qRef = system.referenceProcess.exchanges.stream()
			.filter(e -> e.internalId == exchangeId)
			.findAny()
			.orElse(null);
		if (qRef == null || qRef.flow == null) {
			clearQRef.run();
			return;
		}

		system.referenceExchange = qRef;
		var quantity = Quantity.of(qRef.flow)
			.withProperty(proto.getTargetFlowProperty())
			.withUnit(proto.getTargetUnit())
			.get();
		system.targetFlowPropertyFactor = quantity.factor();
		system.targetUnit = quantity.unit();
	}

	private void addParameterSets(ProductSystem sys, ProtoProductSystem proto) {
		sys.parameterSets.clear();
		for (int i = 0; i < proto.getParameterSetsCount(); i++) {
			var protoSet = proto.getParameterSets(i);
			var set = new ParameterRedefSet();
			set.name = protoSet.getName();
			set.description = protoSet.getDescription();
			set.isBaseline = protoSet.getIsBaseline();
			var redefs = Util.parameterRedefsOf(
				protoSet.getParametersList(), resolver);
			set.parameters.addAll(redefs);
			sys.parameterSets.add(set);
		}
	}

	private Descriptor addProcess(ProductSystem sys, ProtoRef ref) {
		var refId = ref.getId();
		var d = processes.get(refId);
		if (d != null)
			return d;

		var clazz = switch (ref.getType()) {
			case ProductSystem -> ProductSystem.class;
			case Result -> Result.class;
			default -> Process.class;
		};

		var p = resolver.getDescriptor(clazz, refId);
		if (p == null)
			return null;
		processes.put(refId, p);
		sys.processes.add(p.id);
		return p;
	}

	private void resolveLinks(ProductSystem system, List<LinkRef> refs) {
		var db = resolver.db();
		if (db == null)
			return;

		var processIds = new TLongHashSet();
		var flowIds = new TLongHashSet();
		for (var ref : refs) {
			processIds.add(ref.provider.id);
			processIds.add(ref.process.id);
			flowIds.add(ref.flow.id);
		}

		var map = new TLongObjectHashMap<TIntLongHashMap>();
		var sql = "select id, internal_id, f_owner, f_flow from tbl_exchanges";
		NativeSql.on(db).query(sql, r -> {
			long processId = r.getLong(3);
			if (!processIds.contains(processId))
				return true;
			long flowId = r.getLong(4);
			if (!flowIds.contains(flowId))
				return true;

			long eid = r.getLong(1);
			int iid = r.getInt(2);
			var imap = map.get(processId);
			if (imap == null) {
				imap = new TIntLongHashMap(
					Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1, -1);
				map.put(processId, imap);
			}
			imap.put(iid, eid);
			return true;
		});

		for (var ref : refs) {
			var link = ref.resolve(map);
			if (link != null) {
				system.processLinks.add(link);
			}
		}
	}

	private record LinkRef(
		Descriptor provider,
		Descriptor process,
		Descriptor flow,
		int exchange) {

		ProcessLink resolve(TLongObjectHashMap<TIntLongHashMap> map) {
			long processId = process.id;
			var imap = map.get(processId);
			if (imap == null)
				return null;
			long exchangeId = imap.get(exchange);
			if (exchangeId < 0)
				return null;
			var link = new ProcessLink();
			link.providerId = provider.id;
			link.providerType = ProviderType.of(provider.type);
			link.processId = processId;
			link.flowId = flow.id;
			link.exchangeId = exchangeId;
			return link;
		}

	}
}
