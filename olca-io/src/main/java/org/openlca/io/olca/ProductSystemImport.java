package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProductSystemImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ProductSystemDao srcDao;
	private IDatabase source;
	private IDatabase dest;
	private RefSwitcher refs;
	private Sequence seq;

	ProductSystemImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.srcDao = new ProductSystemDao(source);
		this.refs = new RefSwitcher(source, dest, seq);
		this.source = source;
		this.dest = dest;
		this.seq = seq;
	}

	public void run() {
		log.trace("import product systems");
		try {
			for (ProductSystemDescriptor descriptor : srcDao.getDescriptors()) {
				if (seq.contains(seq.PRODUCT_SYSTEM, descriptor.getRefId()))
					continue;
				copy(descriptor);
			}
		} catch (Exception e) {
			log.error("failed to import product systems", e);
		}
	}

	private void copy(ProductSystemDescriptor descriptor) {
		ProductSystem srcSystem = srcDao.getForId(descriptor.getId());
		ProductSystem destSystem = srcSystem.clone();
		destSystem.setRefId(srcSystem.getRefId());
		destSystem.setCategory(refs.switchRef(srcSystem.getCategory()));
		destSystem.referenceProcess = refs.switchRef(srcSystem.referenceProcess);
		switchRefExchange(srcSystem, destSystem);
		destSystem.targetUnit = refs.switchRef(srcSystem.targetUnit);
		switchRefFlowProp(srcSystem, destSystem);
		switchParameterRedefs(destSystem);
		ProductSystemDao destDao = new ProductSystemDao(dest);
		ProductSystemLinks.map(source, dest, destSystem);
		destSystem = destDao.insert(destSystem);
		seq.put(seq.PRODUCT_SYSTEM, srcSystem.getRefId(), destSystem.getId());
	}

	private void switchRefExchange(ProductSystem srcSystem,
			ProductSystem destSystem) {
		Exchange srcExchange = srcSystem.referenceExchange;
		Process destProcess = destSystem.referenceProcess;
		if (srcExchange == null || destProcess == null)
			return;
		Exchange destRefExchange = null;
		for (Exchange destExchange : destProcess.getExchanges()) {
			if (sameExchange(srcExchange, destExchange)) {
				destRefExchange = destExchange;
				break;
			}
		}
		destSystem.referenceExchange = destRefExchange;
	}

	private boolean sameExchange(Exchange srcExchange, Exchange destExchange) {
		if (srcExchange.isInput != destExchange.isInput)
			return false;
		Unit srcUnit = srcExchange.unit;
		Unit destUnit = destExchange.unit;
		Flow srcFlow = srcExchange.flow;
		Flow destFlow = destExchange.flow;
		return srcUnit != null && destUnit != null && srcFlow != null
				&& destFlow != null
				&& Strings.nullOrEqual(srcUnit.getRefId(), destUnit.getRefId())
				&& Strings.nullOrEqual(srcFlow.getRefId(), destFlow.getRefId());
	}

	private void switchRefFlowProp(ProductSystem srcSystem,
			ProductSystem destSystem) {
		Flow destFlow = destSystem.referenceExchange.flow;
		if (destFlow == null)
			return;
		destSystem.targetFlowPropertyFactor = refs.switchRef(
		srcSystem.targetFlowPropertyFactor, destFlow);
	}

	private void switchParameterRedefs(ProductSystem destSystem) {
		for (ParameterRedef redef : destSystem.parameterRedefs) {
			Long contextId = redef.getContextId();
			if (contextId == null)
				continue;
			if (redef.getContextType() == ModelType.IMPACT_METHOD) {
				Long destMethodId = refs.getDestImpactMethodId(contextId);
				redef.setContextId(destMethodId);
			} else {
				long destProcessId = refs.getDestProcessId(contextId);
				redef.setContextId(destProcessId);
			}
		}
	}
}
