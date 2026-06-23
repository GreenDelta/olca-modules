package org.openlca.io.olca.migration;

import java.util.Objects;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ProviderType;
import org.openlca.io.olca.TransferContext;

public class SystemTransferUtil {

	private SystemTransferUtil() {
	}

	/// Maps the reference process, exchange, flow property and unit of the
	/// quantitative reference of the product system.
	public static void swapQRef(
		TransferContext ctx, ProductSystem src, ProductSystem dest
	) {
		if (ctx == null || src == null || dest == null)
			return;
		dest.referenceProcess = ctx.resolve(src.referenceProcess);
		dest.referenceExchange = findMatch(
			src.referenceExchange, dest.referenceProcess);
		var refFlow = dest.referenceExchange != null
			? dest.referenceExchange.flow
			: null;
		if (refFlow == null) {
			dest.targetFlowPropertyFactor = null;
			dest.targetUnit = null;
			return;
		}
		dest.targetFlowPropertyFactor = ctx.mapFactor(
			refFlow, src.targetFlowPropertyFactor);
		dest.targetUnit = ctx.mapUnit(
			dest.targetFlowPropertyFactor, src.targetUnit);
		dest.targetAmount = src.targetAmount;
	}

	/// Maps the process references and product system links from the source
	/// database to the target database.
	public static void swapProcessLinks(
		TransferContext ctx, ProductSystem src, ProductSystem dest
	) {
		if (ctx == null || dest == null)
			return;
		var seq = ctx.seq();

		// map processes
		dest.processes.clear();
		var types = new ModelType[] {
			ModelType.PROCESS, ModelType.RESULT, ModelType.PRODUCT_SYSTEM
		};
		for (var srcId : src.processes) {
			for (var type : types) {
				long destId = seq.get(type, srcId);
				if (destId > 0) {
					dest.processes.add(destId);
					break;
				}
			}
		}

		// map process links
		var exchanges = ExchangeFinder.of(ctx);
		for (var link : dest.processLinks) {
			link.processId = seq.get(ModelType.PROCESS, link.processId);
			var providerType = ProviderType.toModelType(link.providerType);
			link.providerId = seq.get(providerType, link.providerId);
			link.flowId = seq.get(ModelType.FLOW, link.flowId);
			link.exchangeId = exchanges.find(link);
		}
	}

	/// Tries to find the corresponding exchange in `destProcess` that
	/// matches `srcExchange`.
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
