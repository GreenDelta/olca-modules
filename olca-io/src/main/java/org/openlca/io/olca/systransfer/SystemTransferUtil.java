package org.openlca.io.olca.systransfer;

import java.util.Objects;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.io.olca.TransferContext;

public class SystemTransferUtil {

	private SystemTransferUtil() {
	}

	public static void swapQRef(
		TransferContext ctx, ProductSystem src, ProductSystem dest
	) {
		if (ctx == null || src == null || dest == null)
			return;
		dest.referenceExchange = findMatch(src.referenceExchange, dest.referenceProcess);
		var refFlow = dest.referenceExchange != null
			? dest.referenceExchange.flow
			: null;
		if (refFlow == null) {
			dest.targetFlowPropertyFactor = null;
			dest.targetUnit = null;
			return;
		}
		dest.targetFlowPropertyFactor = ctx.mapFactor(refFlow, src.targetFlowPropertyFactor);
		dest.targetUnit = ctx.mapUnit(dest.targetFlowPropertyFactor, src.targetUnit);
	}

	/// Tries to find the corresponding
	private static Exchange findMatch(Exchange srcExchange, Process destProcess) {
		if (srcExchange == null || destProcess == null)
			return null;

		// fast path for copied processes
		var candidate = destProcess.getExchange(srcExchange.internalId);
		if (same(srcExchange, candidate))
			return candidate;

		// fallback for matched processes
		return destProcess.exchanges.stream()
			.filter(e -> same(srcExchange, e))
			.findAny()
			.orElse(null);
	}

	private static boolean same(Exchange a, Exchange b) {
		if (a == null || b == null || a.isInput != b.isInput)
			return false;
		return a.unit != null && b.unit != null
			&& a.flow != null && b.flow != null
			&& Objects.equals(a.unit.refId, b.unit.refId)
			&& Objects.equals(a.flow.refId, b.flow.refId);
	}

}
