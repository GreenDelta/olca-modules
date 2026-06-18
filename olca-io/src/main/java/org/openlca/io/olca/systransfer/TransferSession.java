package org.openlca.io.olca.systransfer;

import java.util.HashMap;
import java.util.Map;

import org.openlca.commons.Res;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ProviderType;
import org.openlca.io.olca.TransferContext;

record TransferSession(
	TransferPlan plan,
	TransferContext context,
	ExchangeFinder exchanges,
	Map<Long, TypeMapping> providerTypes
) {

	record TypeMapping(ModelType originalType, byte mappedType) {
	}

	static Res<TransferSession> create(TransferPlan plan) {

		if (plan == null
			|| plan.config() == null
			|| plan.config().isNotComplete())
			return Res.error("Incomplete transfer configuration");

		try {
			var context = TransferContext.create(
				plan.config().source(), plan.config().target());
			var seq = context.seq();
			for (var match : plan.matches()) {
				if (!match.isComplete())
					continue;
				seq.put(
					match.source().provider().type,
					match.source().provider().id,
					match.selected().provider().id);
			}

			var session = new TransferSession(
				plan, context, ExchangeFinder.of(context), typeMappingsOf(plan));
			return Res.ok(session);
		} catch (Exception e) {
			return Res.error("Failed to create the transfer session", e);
		}
	}

	private static Map<Long, TypeMapping> typeMappingsOf(TransferPlan plan) {
		var map = new HashMap<Long, TypeMapping>();
		for (var match : plan.matches()) {
			if (!match.isComplete())
				continue;
			var originalType = match.source().provider().type;
			byte mappedType = ProviderType.of(match.selected().provider().type);
			map.put(match.source().provider().id,
				new TypeMapping(originalType, mappedType));
		}
		for (var copy : plan.copies()) {
			if (copy.provider() == null || copy.provider().type == null)
				continue;
			var type = copy.provider().type;
			map.put(copy.provider().id,
				new TypeMapping(type, ProviderType.of(type)));
		}
		return map;
	}

	void copyAnalysisGroups(ProductSystem origin, ProductSystem copy) {
		var seq = context.seq();
		for (var group : origin.analysisGroups) {
			var groupCopy = group.copy();
			groupCopy.processes.clear();
			for (var oid : group.processes) {
				var mapping = providerTypes.get(oid);
				if (mapping == null)
					continue;
				long mappedId = seq.get(mapping.originalType(), oid);
				if (mappedId > 0 && copy.processes.contains(mappedId)) {
					groupCopy.processes.add(mappedId);
				}
			}
			copy.analysisGroups.add(groupCopy);
		}
	}

	void transferCopies() {
		for (var p : plan.copies()) {
			if (p.provider() == null || p.provider().type == null)
				continue;
			var entity = context.source().get(
				p.provider().type.getModelClass(), p.provider().id);
			context.resolve(entity);
		}
		swapDefaultProviders();
	}

	ProcessLink copyLink(ProcessLink origin) {
		var target = origin.copy();
		var seq = context.seq();

		// map provider
		var originalType = ProviderType.toModelType(origin.providerType);
		target.providerId = seq.get(originalType, origin.providerId);
		var mapping = providerTypes.get(origin.providerId);
		if (mapping != null) {
			target.providerType = mapping.mappedType();
		}

		// map flow, process, and exchange
		target.flowId = seq.get(ModelType.FLOW, origin.flowId);
		target.processId = seq.get(ModelType.PROCESS, origin.processId);
		target.exchangeId = exchanges.find(origin);

		return target;
	}

	/// Same as in `ProcessTransfer.swapDefaultProviders` but here we need to
	/// consider that the type of a provider could change, for example when a
	/// process in the source database is mapped to a precalculated result in
	/// the target database.
	private void swapDefaultProviders() {
		var q = "select f_default_provider, default_provider_type "
			+ "from tbl_exchanges where f_default_provider < 0";
		NativeSql.on(context.target()).updateRows(q, r -> {
			long sourceId = Math.abs(r.getLong(1));
			var storedType = ProviderType.toModelType(r.getByte(2));
			long targetId = context.seq().get(storedType, sourceId);
			if (targetId > 0) {
				r.updateLong(1, targetId);
				var mapping = providerTypes.get(sourceId);
				if (mapping != null) {
					r.updateByte(2, mapping.mappedType());
				}
				r.updateRow();
			}
			return true;
		});
	}
}
