package org.openlca.io.olca;

import gnu.trove.map.hash.TLongLongHashMap;
import org.openlca.core.database.BaseDao;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProductSystemImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ProductSystemDao srcDao;
	private IDatabase source;
	private IDatabase dest;
	private Sequence seq;

	private TLongLongHashMap processMap = new TLongLongHashMap();
	private TLongLongHashMap flowMap = new TLongLongHashMap();

	ProductSystemImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.srcDao = new ProductSystemDao(source);
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
		switchRefUnit(srcSystem, destSystem);
		switchRefFlowProp(srcSystem, destSystem);
		switchProcessIds(srcSystem, destSystem);
		switchProcessLinkIds(destSystem);
		switchParameterRedefs(destSystem);
		ProductSystemDao destDao = new ProductSystemDao(dest);
		destSystem = destDao.insert(destSystem);
		seq.put(seq.PRODUCT_SYSTEM, srcSystem.getRefId(), destSystem.getId());
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
		if (srcExchange.isInput() != destExchange.isInput())
			return false;
		Unit srcUnit = srcExchange.getUnit();
		Unit destUnit = destExchange.getUnit();
		Flow srcFlow = srcExchange.getFlow();
		Flow destFlow = destExchange.getFlow();
		return srcUnit != null && destUnit != null
				&& srcFlow != null && destFlow != null
				&& Strings.nullOrEqual(srcUnit.getRefId(), destUnit.getRefId())
				&& Strings.nullOrEqual(srcFlow.getRefId(), destFlow.getRefId());
	}

	private void switchRefUnit(ProductSystem srcSystem, ProductSystem destSystem) {
		if (srcSystem.getTargetUnit() == null)
			return;
		long id = seq.get(seq.UNIT, srcSystem.getTargetUnit().getRefId());
		BaseDao<Unit> dao = dest.createDao(Unit.class);
		destSystem.setTargetUnit(dao.getForId(id));
	}

	private void switchRefFlowProp(ProductSystem srcSystem,
	                               ProductSystem destSystem) {
		FlowPropertyFactor srcFac = srcSystem.getTargetFlowPropertyFactor();
		if (srcFac == null || srcFac.getFlowProperty() == null
				|| destSystem.getReferenceExchange() == null)
			return;
		Flow destFlow = destSystem.getReferenceExchange().getFlow();
		if (destFlow == null)
			return;
		FlowPropertyFactor fac = destFlow.getFactor(srcFac.getFlowProperty());
		destSystem.setTargetFlowPropertyFactor(fac);
	}

	private void switchProcessIds(ProductSystem srcSystem,
	                              ProductSystem destSystem) {
		destSystem.getProcesses().clear();
		for (long srcProcessId : srcSystem.getProcesses()) {
			long destProcessId = processMap.get(srcProcessId);
			if (destProcessId == 0L)
				continue;
			destSystem.getProcesses().add(destProcessId);
		}
	}

	private void switchProcessLinkIds(ProductSystem destSystem) {
		for (ProcessLink link : destSystem.getProcessLinks()) {
			long destProviderId = processMap.get(link.getProviderId());
			long destFlowId = flowMap.get(link.getFlowId());
			long destRecipientId = processMap.get(link.getRecipientId());
			if (destProviderId == 0 || destFlowId == 0 || destRecipientId == 0)
				log.warn("could not translate process link {}", link);
			link.setProviderId(destProviderId);
			link.setFlowId(destFlowId);
			link.setRecipientId(destRecipientId);
		}
	}

	private void switchParameterRedefs(ProductSystem destSystem) {
		 for(ParameterRedef redef : destSystem.getParameterRedefs()) {
			 long destProcessId = processMap.get(redef.getContextId());
			 redef.setContextId(destProcessId);
		 }
	}
}
