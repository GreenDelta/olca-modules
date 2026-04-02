package org.openlca.io.olca;

import java.util.Objects;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;

final class ProductSystemTransfer implements EntityTransfer<ProductSystem> {

	private final TransferConfig conf;
	private final SeqMap seq;

	ProductSystemTransfer(TransferConfig conf) {
		this.conf = conf;
		this.seq = conf.seq();
	}

	@Override
	public void syncAll() {
		for (var d : conf.source().getDescriptors(ProductSystem.class)) {
			var origin = conf.source().get(ProductSystem.class, d.id);
			sync(origin);
		}
	}

	@Override
	public ProductSystem sync(ProductSystem origin) {
		return conf.sync(origin, () -> {
			var copy = origin.copy();
			copy.referenceProcess = conf.swap(origin.referenceProcess);
			swapQRef(origin, copy);
			swapParameters(copy);
			swapAnalysisGroups(copy);
			ProductSystemLinks.map(conf, copy);
			return copy;
		});
	}

	private void swapQRef(ProductSystem src, ProductSystem copy) {
		if (src.referenceExchange == null || copy.referenceProcess == null)
			return;
		copy.referenceExchange = copy.referenceProcess.exchanges.stream()
				.filter(e -> isSame(src.referenceExchange, e))
				.findAny()
				.orElse(null);
		var refFlow = copy.referenceExchange != null
				? copy.referenceExchange.flow
				: null;
		if (refFlow == null)
			return;
		copy.targetFlowPropertyFactor =
				conf.mapFactor(refFlow, src.targetFlowPropertyFactor);
		copy.targetUnit =
				conf.mapUnit(copy.targetFlowPropertyFactor, src.targetUnit);
	}

	private boolean isSame(Exchange e, Exchange copy) {
		if (e.isInput != copy.isInput)
			return false;
		return e.unit != null && copy.unit != null
			&& e.flow != null && copy.flow != null
			&& Objects.equals(e.unit.refId, copy.unit.refId)
			&& Objects.equals(e.flow.refId, copy.flow.refId);
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
