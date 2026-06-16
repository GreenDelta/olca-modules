package org.openlca.io.olca.systransfer;

import org.openlca.commons.Res;
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
				// TODO: we need to support type switches
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

}
