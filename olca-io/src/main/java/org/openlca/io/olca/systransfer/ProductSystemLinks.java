package org.openlca.io.olca.systransfer;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.io.olca.TransferContext;
import org.openlca.util.RefIdMap;

/**
 * Maps the process IDs and IDs of the product system links.
 */
public class ProductSystemLinks {

	private final ProductSystem system;
	private final RefIdMap<Long, String> srcIdMap;
	private final RefIdMap<String, Long> destIdMap;
	private final ExchangeFinder exchanges;

	public static void map(TransferContext conf, ProductSystem system) {
		if (system == null)
			return;
		new ProductSystemLinks(conf, system).map();
	}

	private ProductSystemLinks(TransferContext ctx, ProductSystem system) {
		this.system = system;
		srcIdMap = RefIdMap.internalToRef(
				ctx.source(), Process.class, Flow.class, Unit.class);
		destIdMap = RefIdMap.refToInternal(
				ctx.target(), Process.class, Flow.class, Unit.class);
		exchanges = ExchangeFinder.of(ctx);
	}

	private long destId(Class<?> type, long sourceId) {
		if (sourceId == 0)
			return 0;
		String refId = srcIdMap.get(type, sourceId);
		if (refId == null)
			return 0;
		Long destId = destIdMap.get(type, refId);
		return destId == null ? 0 : destId;
	}

	private void map() {
		mapProcessIds();
		for (var link : system.processLinks) {
			link.providerId = destId(Process.class, link.providerId);
			link.processId = destId(Process.class, link.processId);
			link.flowId = destId(Flow.class, link.flowId);
			link.exchangeId = exchanges.find(link);
		}
	}

	private void mapProcessIds() {
		List<Long> destProcessIds = new ArrayList<>();
		for (Long id : system.processes) {
			destProcessIds.add(destId(Process.class, id));
		}
		system.processes.clear();
		system.processes.addAll(destProcessIds);
	}
}
