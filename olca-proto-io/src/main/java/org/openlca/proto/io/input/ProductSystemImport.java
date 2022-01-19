package org.openlca.proto.io.input;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.proto.ProtoProductSystem;
import org.openlca.util.Strings;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

record ProductSystemImport(ProtoImport imp) implements Import<ProductSystem> {

	@Override
	public ImportStatus<ProductSystem> of(String id) {
		var sys = imp.get(ProductSystem.class, id);

		// check if we are in update mode
		var update = false;
		if (sys != null) {
			update = imp.shouldUpdate(sys);
			if (!update) {
				return ImportStatus.skipped(sys);
			}
		}

		// resolve the proto object
		var proto = imp.reader.getProductSystem(id);
		if (proto == null)
			return sys != null
				? ImportStatus.skipped(sys)
				: ImportStatus.error(
				"Could not resolve ProductSystem " + id);

		var wrap = ProtoWrap.of(proto);
		if (update) {
			if (imp.skipUpdate(sys, wrap))
				return ImportStatus.skipped(sys);
		}

		// map the data
		if (sys == null) {
			sys = new ProductSystem();
		}
		wrap.mapTo(sys, imp);
		map(proto, sys);

		// insert or update it
		var dao = new ProductSystemDao(imp.db);
		sys = update
			? dao.update(sys)
			: dao.insert(sys);
		imp.putHandled(sys);
		return update
			? ImportStatus.updated(sys)
			: ImportStatus.created(sys);
	}

	private void map(ProtoProductSystem proto, ProductSystem sys) {
		mapQRef(proto, sys);
		mapLinks(proto, sys);
		// map parameters after the links and processes
		// because we may need the process IDs in redefinitions
		// this also means that LCIA methods with redefined
		// parameters should be already contained in the database
		mapParameters(proto, sys);
	}

	private void mapLinks(ProtoProductSystem proto, ProductSystem sys) {
		// sync processes ; todo: this must contain also product systems and results
		var processes = syncProcesses(proto);
		sys.processes.clear();
		processes.values().forEach(d -> sys.processes.add(d.id));

		// collecting the flows
		var flows = new FlowDao(imp.db)
			.getDescriptors()
			.stream()
			.filter(f -> f.flowType != FlowType.ELEMENTARY_FLOW)
			.collect(Collectors.toMap(
				d -> d.refId,
				d -> d
			));

		// we index the process links first in a structure
		// process ID -> internal exchange ID -> link
		// after we have created and indexed the links, we
		// scan the exchange table and assign the database
		// internal IDs to the links and add them to the
		// product system.
		var index = new TLongObjectHashMap<TIntObjectHashMap<ProcessLink>>();

		for (var protoLink : proto.getProcessLinksList()) {
			var link = new ProcessLink();

			// provider
			var provider = processes.get(protoLink.getProvider().getId());
			if (provider == null)
				continue;
			link.providerId = provider.id;
			link.setProviderType(provider.type);

			// flow
			var flow = flows.get(protoLink.getFlow().getId());
			if (flow == null)
				continue;
			link.flowId = flow.id;

			// process
			var process = processes.get(protoLink.getProcess().getId());
			if (process == null)
				continue;
			link.processId = process.id;

			// exchange
			var internalID = protoLink.getExchange().getInternalId();
			if (internalID == 0)
				continue;

			// index the link
			var idx = index.get(process.id);
			if (idx == null) {
				idx = new TIntObjectHashMap<>();
				index.put(process.id, idx);
			}
			idx.put(internalID, link);
		}

		// add the indexed links
		var sql = "select id, f_owner, internal_id from tbl_exchanges";
		NativeSql.on(imp.db).query(sql, r -> {
			var processID = r.getLong(2);
			var idx = index.get(processID);
			if (idx == null)
				return true;

			var internalID = r.getInt(3);
			var link = idx.remove(internalID);
			if (link == null)
				return true;

			link.exchangeId = r.getLong(1);
			sys.processLinks.add(link);
			return true;
		});
	}

	private Map<String, Descriptor> syncProcesses(ProtoProductSystem proto) {
		var map = new HashMap<String, Descriptor>();

		// handles a process (or product system) reference
		BiConsumer<String, Boolean> handleRef = (refID, checkForSystem) -> {
			if (Strings.nullOrEmpty(refID))
				return;
			if (map.containsKey(refID))
				return;

			var process = new ProcessImport(imp).of(refID).model();
			if (process != null) {
				map.put(refID, Descriptor.of(process));
				return;
			}

			// providers of links can also be product systems
			if (checkForSystem) {
				var sys = new ProductSystemImport(imp)
					.of(refID)
					.model();
				if (sys != null) {
					map.put(refID, Descriptor.of(sys));
				}
			}
		};

		handleRef.accept(proto.getReferenceProcess().getId(), false);
		for (var ref : proto.getProcessesList()) {
			handleRef.accept(ref.getId(), true);
		}
		for (var link : proto.getProcessLinksList()) {
			handleRef.accept(link.getProvider().getId(), true);
			handleRef.accept(link.getProcess().getId(), false);
		}
		return map;
	}

	private void mapQRef(ProtoProductSystem proto, ProductSystem sys) {

		// ref. process
		sys.referenceProcess = ProcessImport
			.of(imp, proto.getReferenceProcess().getId())
			.model();

		// ref. exchange
		var refExchange = proto.getReferenceExchange().getInternalId();
		if (sys.referenceProcess != null && refExchange > 0) {
			sys.referenceExchange = sys.referenceProcess.exchanges.stream()
				.filter(e -> e.internalId == refExchange)
				.findAny()
				.orElse(null);
		}

		// ref. quantity
		var qref = sys.referenceExchange;
		if (qref != null && qref.flow != null) {
			var propID = proto.getTargetFlowProperty().getId();
			sys.targetFlowPropertyFactor = Strings.nullOrEmpty(propID)
				? qref.flowPropertyFactor
				: qref.flow.flowPropertyFactors.stream()
				.filter(f -> f.flowProperty != null
					&& Strings.nullOrEqual(f.flowProperty.refId, propID))
				.findAny()
				.orElse(null);
		}

		// ref. unit
		var qf = sys.targetFlowPropertyFactor;
		if (qref != null
			&& qf != null
			&& qf.flowProperty != null
			&& qf.flowProperty.unitGroup != null) {
			var group = qf.flowProperty.unitGroup;
			var unitID = proto.getTargetUnit().getId();
			sys.targetUnit = Strings.nullOrEmpty(unitID)
				? qref.unit
				: group.units.stream()
				.filter(u -> Strings.nullOrEqual(unitID, u.refId))
				.findAny()
				.orElse(null);
		}

		sys.targetAmount = proto.getTargetAmount();
	}

	private void mapParameters(ProtoProductSystem proto, ProductSystem sys) {
		// note that global parameters need to be already imported into
		// the database; also we assume that the processes and impact
		// categories are already available in the database here.
		for (var protoSet : proto.getParameterSetsList()) {
			var set = new ParameterRedefSet();
			sys.parameterSets.add(set);
			set.name = protoSet.getName();
			set.description = protoSet.getDescription();
			set.isBaseline = protoSet.getIsBaseline();
			for (var protoRedef : protoSet.getParametersList()) {
				var redef = In.parameterRedefOf(protoRedef, imp.db);
				set.parameters.add(redef);
			}
		}
	}

}
