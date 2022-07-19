package org.openlca.jsonld.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.JsonObject;
import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.NativeSql;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Result;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.jsonld.Json;

public class ProductSystemReader implements EntityReader<ProductSystem> {

	private final EntityResolver resolver;
	private final Map<String, Descriptor> processes = new HashMap<>();

	public ProductSystemReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public ProductSystem read(JsonObject json) {
		var system = new ProductSystem();
		update(system, json);
		return system;
	}

	@Override
	public void update(ProductSystem system, JsonObject json) {

		// clear resources in case the reader is re-used
		processes.clear();

		Util.mapBase(system, json, resolver);
		mapQRef(json, system);
		addParameterSets(json, system);

		// add processes and cache them
		system.processes.clear();
		Json.forEachObject(
			json, "processes", obj -> addProcess(system, obj));

		// add link references and cache them
		system.processLinks.clear();
		var linkRefs = new ArrayList<LinkRef>();
		Json.forEachObject(json, "processLinks", obj -> {
			var provider = addProcess(system, Json.getObject(obj, "provider"));
			var process = addProcess(system, Json.getObject(obj, "process"));
			String flowRefId = Json.getRefId(obj, "flow");
			var flow = resolver.getDescriptor(Flow.class, flowRefId);
			var exchangeRef = Json.getObject(obj, "exchange");
			var exchangeId = Json.getInt(exchangeRef, "internalId");
			if (provider == null
				|| process == null
				|| flow == null
				|| exchangeId.isEmpty())
				return;
			linkRefs.add(new LinkRef(provider, process, flow, exchangeId.getAsInt()));
		});

		resolveLinks(system, linkRefs);
	}

	private void mapQRef(JsonObject json, ProductSystem system) {
		var refProcessId = Json.getRefId(json, "refProcess");
		system.targetAmount = Json.getDouble(json, "targetAmount", 1d);
		system.referenceProcess = resolver.get(Process.class, refProcessId);

		Runnable clearQRef = () -> {
			system.referenceExchange = null;
			system.targetFlowPropertyFactor = null;
			system.targetUnit = null;
		};

		if (system.referenceProcess == null) {
			clearQRef.run();
			return;
		}

		Exchange qRef = null;
		var exchangeRef = Json.getObject(json, "refExchange");
		if (exchangeRef != null) {
			var exchangeId = Json.getInt(exchangeRef, "internalId");
			if (exchangeId.isEmpty()) {
				clearQRef.run();
				return;
			}
			var eid = exchangeId.getAsInt();
			qRef = system.referenceProcess.exchanges.stream()
				.filter(e -> e.internalId == eid)
				.findAny()
				.orElse(null);
			if (qRef == null) {
				clearQRef.run();
				return;
			}
		}

		system.referenceExchange = qRef == null
			? system.referenceProcess.quantitativeReference
			: null;
		if (qRef == null || qRef.flow == null) {
			clearQRef.run();
			return;
		}

		var quantity = Quantity.of(
			qRef.flow, json, "targetFlowProperty", "targetUnit");
		system.targetFlowPropertyFactor = quantity.factor();
		system.targetUnit = quantity.unit();
	}

	private void addParameterSets(JsonObject json, ProductSystem sys) {
		sys.parameterSets.clear();
		Json.forEachObject(json, "parameterSets", obj -> {
			var set = new ParameterRedefSet();
			sys.parameterSets.add(set);
			set.name = Json.getString(obj, "name");
			set.description = Json.getString(obj, "description");
			set.isBaseline = Json.getBool(obj, "isBaseline", false);
			var redefs = Json.getArray(obj, "parameters");
			if (redefs != null && redefs.size() > 0) {
				set.parameters.addAll(ParameterRedefs.read(redefs, resolver));
			}
		});
	}

	private Descriptor addProcess(ProductSystem s, JsonObject ref) {
		if (ref == null)
			return null;
		String refId = Json.getString(ref, "@id");
		if (refId == null)
			return null;
		var d = processes.get(refId);
		if (d != null)
			return d;

		String type = Json.getString(ref, "@type");
		var clazz = type == null
			? Process.class
			: switch (type) {
			case "ProductSystem" -> ProductSystem.class;
			case "Result" -> Result.class;
			default -> Process.class;
		};

		var p = resolver.getDescriptor(clazz, refId);
		if (p == null)
			return null;
		processes.put(refId, p);
		s.processes.add(p.id);
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
			link.setProviderType(provider.type);
			link.processId = processId;
			link.flowId = flow.id;
			link.exchangeId = exchangeId;
			return link;
		}

	}
}
