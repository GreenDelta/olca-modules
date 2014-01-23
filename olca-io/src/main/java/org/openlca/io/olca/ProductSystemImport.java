package org.openlca.io.olca;

import gnu.trove.map.hash.TLongLongHashMap;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProductSystemImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ProductSystemDao srcDao;
	private ProductSystemDao destDao;
	private IDatabase source;
	private IDatabase dest;
	private Sequence seq;

	private TLongLongHashMap processMap = new TLongLongHashMap();
	private TLongLongHashMap flowMap = new TLongLongHashMap();

	ProductSystemImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.srcDao = new ProductSystemDao(source);
		this.destDao = new ProductSystemDao(dest);
		this.source = source;
		this.dest = dest;
		this.seq = seq;
	}

	public void run() {
		log.trace("import product systems");
		try {
			buildProcessMap();
			buildFlowMap();
			for (ProductSystemDescriptor descriptor : srcDao.getDescriptors()) {
				if (seq.contains(seq.PRODUCT_SYSTEM, descriptor.getRefId()))
					continue;
				createSystem(descriptor);
			}
		} catch (Exception e) {
			log.error("failed to import product systems", e);
		}
	}

	private void buildProcessMap() {
		ProcessDao srcDao = new ProcessDao(source);
		for (ProcessDescriptor descriptor : srcDao.getDescriptors()) {
			long srcId = descriptor.getId();
			long destId = seq.get(seq.PROCESS, descriptor.getRefId());
			processMap.put(srcId, destId);
		}
	}

	private void buildFlowMap() {
		FlowDao srcDao = new FlowDao(source);
		for (FlowDescriptor descriptor : srcDao.getDescriptors()) {
			long srcId = descriptor.getId();
			long destId = seq.get(seq.FLOW, descriptor.getRefId());
			flowMap.put(srcId, destId);
		}
	}

	private void createSystem(ProductSystemDescriptor descriptor) {
		ProductSystem srcSystem = srcDao.getForId(descriptor.getId());
		ProductSystem destSystem = srcSystem.clone();
		destSystem.setRefId(srcSystem.getRefId());
		switchCategory(srcSystem, destSystem);
		switchRefProcess(srcSystem, destSystem);
		switchRefExchange(srcSystem, destSystem);
		// TODO: switchRefFlowProperty
		// TODO: switchRefUnit
		// TODO: switchTargetAmount
		// TODO: switchProcessIds
		// TODO: switchProcessLinks
	}

	private void switchCategory(ProductSystem srcSystem, ProductSystem
			destSystem) {
		if (srcSystem.getCategory() == null)
			return;
		long catId = seq.get(seq.CATEGORY, srcSystem.getCategory().getRefId());
		CategoryDao destDao = new CategoryDao(dest);
		destSystem.setCategory(destDao.getForId(catId));
	}

	private void switchRefProcess(ProductSystem srcSystem, ProductSystem
			destSystem) {
		if (srcSystem.getReferenceProcess() == null)
			return;
		long destId = seq.get(seq.PROCESS, srcSystem.getReferenceProcess()
				.getRefId());
		ProcessDao destDao = new ProcessDao(dest);
		destSystem.setReferenceProcess(destDao.getForId(destId));
	}

	private void switchRefExchange(ProductSystem srcSystem, ProductSystem
			destSystem) {
		Exchange srcExchange = srcSystem.getReferenceExchange();
		Process destProcess = destSystem.getReferenceProcess();
		if (srcExchange == null || destProcess == null)
			return;
		Exchange destRefExchange = null;
		for (Exchange destExchange : destProcess.getExchanges()) {
			if (sameExchange(srcExchange, destExchange)) {
				destRefExchange = destExchange;
				break;
			}
		}
		destSystem.setReferenceExchange(destRefExchange);
	}

	private boolean sameExchange(Exchange srcExchange, Exchange destExchange) {
		return false; // TODO: not yet implemented
	}


}
