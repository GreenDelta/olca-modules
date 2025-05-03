package org.openlca.io.olca;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.util.Strings;

class ProductSystemImport {

	private final Config conf;
	private final SeqMap seq;
	private final RefSwitcher refs;

	private ProductSystemImport(Config conf) {
		this.conf = conf;
		this.seq = conf.seq();
		this.refs = new RefSwitcher(conf);
	}

	static void run(Config conf) {
		new ProductSystemImport(conf).run();
	}

	private void run() {
		conf.syncAll(ProductSystem.class, system -> {
			var copy = system.copy();
			copy.referenceProcess = conf.swap(system.referenceProcess);
			swapQRef(system, copy);
			swapParameters(copy);
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
				refs.switchRef(src.targetFlowPropertyFactor, refFlow);
		copy.targetUnit = src.targetUnit != null
				? Config.findUnit(copy.targetFlowPropertyFactor, src.targetUnit.refId)
				: null;
	}

	private boolean isSame(Exchange e, Exchange copy) {
		if (e.isInput != copy.isInput)
			return false;
		return e.unit != null && copy.unit != null
				&& e.flow != null && copy.flow != null
				&& Strings.nullOrEqual(e.unit.refId, copy.unit.refId)
				&& Strings.nullOrEqual(e.flow.refId, copy.flow.refId);
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
}
