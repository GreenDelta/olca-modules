package org.openlca.io.olca;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.io.olca.systransfer.SystemTransferUtil;

final class ProductSystemTransfer implements EntityTransfer<ProductSystem> {

	private final TransferContext ctx;
	private final SeqMap seq;

	ProductSystemTransfer(TransferContext ctx) {
		this.ctx = ctx;
		this.seq = ctx.seq();
	}

	@Override
	public void syncAll() {
		for (var d : ctx.source().getDescriptors(ProductSystem.class)) {
			var origin = ctx.source().get(ProductSystem.class, d.id);
			sync(origin);
		}
	}

	@Override
	public ProductSystem sync(ProductSystem origin) {
		return ctx.sync(origin, () -> {
			var copy = origin.copy();
			SystemTransferUtil.swapQRef(ctx, origin, copy);
			SystemTransferUtil.swapProcessLinks(ctx, origin, copy);
			swapParameters(copy);
			swapAnalysisGroups(copy);
			return copy;
		});
	}

	private void swapParameters(ProductSystem copy) {
		for (var set : copy.parameterSets) {
			for (var p : set.parameters) {
				if (p.contextId == null)
					continue;
				p.contextId = p.contextType == ModelType.IMPACT_CATEGORY
						? seq.get(ModelType.IMPACT_CATEGORY, p.contextId)
						: seq.get(ModelType.PROCESS, p.contextId);
			}
		}
	}

	private void swapAnalysisGroups(ProductSystem copy) {
		for (var group : copy.analysisGroups) {
			var mapped = group.processes.stream()
					.map(id -> seq.get(ModelType.PROCESS, id))
					.filter(id -> id != 0)
					.toList();
			group.processes.clear();
			group.processes.addAll(mapped);
		}
	}
}
