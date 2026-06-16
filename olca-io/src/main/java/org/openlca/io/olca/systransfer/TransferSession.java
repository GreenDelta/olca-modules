package org.openlca.io.olca.systransfer;

import java.util.HashMap;

import org.openlca.commons.Res;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ProviderType;
import org.openlca.io.olca.TransferContext;

record TransferSession(
	TransferPlan plan,
	TransferContext context
) {

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
				if (match.provider() == null
					|| match.provider().provider() == null
					|| match.selected().provider() == null)
					continue;
				seq.put(
					match.provider().provider().type,
					match.provider().provider().id,
					match.selected().provider().id);
			}

			var session = new TransferSession(plan, context);
			return Res.ok(session);
		} catch (Exception e) {
			return Res.error("Failed to create the transfer session", e);
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

	/// Same as in `ProcessTransfer.swapDefaultProviders` but here we need to
	/// consider that the type of a provider could change, for example when a
	/// process in the source database is mapped to a precalculated result in
	/// the target database.
	private void swapDefaultProviders() {

		var typeChanges = new HashMap<Long, Byte>();
		for (var match : plan.matches()) {
			if (match.provider() == null
				|| match.provider().provider() == null
				|| match.selected() == null
				|| match.selected().provider() == null)
				continue;
			var origType = match.provider().provider().type;
			var selType = match.selected().provider().type;
			if (origType != selType) {
				typeChanges.put(
					match.provider().provider().id,
					ProviderType.of(selType));
			}
		}

		var q = "select f_default_provider, default_provider_type "
			+ "from tbl_exchanges where f_default_provider < 0";
		NativeSql.on(context.target()).updateRows(q, r -> {
			long sourceId = Math.abs(r.getLong(1));
			var storedType = ProviderType.toModelType(r.getByte(2));
			long targetId = context.seq().get(storedType, sourceId);
			if (targetId > 0) {
				r.updateLong(1, targetId);
				var newType = typeChanges.get(sourceId);
				if (newType != null) {
					r.updateByte(2, newType);
				}
				r.updateRow();
			}
			return true;
		});
	}
}
