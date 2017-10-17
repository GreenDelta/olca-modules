package org.openlca.io.olca;

import org.openlca.core.database.ActorDao;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.database.SocialIndicatorDao;
import org.openlca.core.database.SourceDao;
import org.openlca.core.database.UnitDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * We copy most of the entities from the source database to the target database
 * by cloning the source entity, changing the references of the source entity to
 * the corresponding references in the destination database, and finally
 * inserting the new entity in the destination database. This class helps to
 * switch a referenced entity from the source database to the corresponding
 * entity in the destination database.
 */
class RefSwitcher {

	private Sequence seq;
	private IDatabase source;
	private IDatabase dest;

	RefSwitcher(IDatabase source, IDatabase dest, Sequence seq) {
		this.source = source;
		this.dest = dest;
		this.seq = seq;
	}

	Category switchRef(Category srcCategory) {
		return switchRef(seq.CATEGORY, new CategoryDao(dest), srcCategory);
	}

	Location switchRef(Location srcLocation) {
		return switchRef(seq.LOCATION, new LocationDao(dest), srcLocation);
	}

	Actor switchRef(Actor srcActor) {
		return switchRef(seq.ACTOR, new ActorDao(dest), srcActor);
	}

	Source switchRef(Source srcSource) {
		return switchRef(seq.SOURCE, new SourceDao(dest), srcSource);
	}

	Unit switchRef(Unit srcUnit) {
		if (srcUnit == null)
			return null;
		long id = seq.get(seq.UNIT, srcUnit.getRefId());
		if (id == 0)
			return null;
		UnitDao dao = new UnitDao(dest);
		return dao.getForId(id);
	}

	UnitGroup switchRef(UnitGroup srcUnitGroup) {
		return switchRef(seq.UNIT_GROUP, new UnitGroupDao(dest), srcUnitGroup);
	}

	FlowProperty switchRef(FlowProperty srcProperty) {
		return switchRef(seq.FLOW_PROPERTY, new FlowPropertyDao(dest), srcProperty);
	}

	Flow switchRef(Flow srcFlow) {
		return switchRef(seq.FLOW, new FlowDao(dest), srcFlow);
	}

	/**
	 * Returns the corresponding flow property factor of the destination flow.
	 */
	FlowPropertyFactor switchRef(FlowPropertyFactor srcFactor, Flow destFlow) {
		if (srcFactor == null || destFlow == null)
			return null;
		FlowProperty srcProp = srcFactor.getFlowProperty();
		if (srcProp == null)
			return null;
		long propId = seq.get(seq.FLOW_PROPERTY, srcProp.getRefId());
		for (FlowPropertyFactor fac : destFlow.getFlowPropertyFactors()) {
			if (fac.getFlowProperty() == null)
				continue;
			if (propId == fac.getFlowProperty().getId())
				return fac;
		}
		return null;
	}

	Currency switchRef(Currency srcCurrency) {
		return switchRef(seq.CURRENCY, new CurrencyDao(dest), srcCurrency);
	}

	Process switchRef(Process srcProcess) {
		return switchRef(seq.PROCESS, new ProcessDao(dest), srcProcess);
	}

	ProductSystem switchRef(ProductSystem srcProductSystem) {
		return switchRef(seq.PRODUCT_SYSTEM, new ProductSystemDao(dest), srcProductSystem);
	}

	SocialIndicator switchRef(SocialIndicator srcIndicator) {
		return switchRef(seq.SOCIAL_INDICATOR, new SocialIndicatorDao(dest), srcIndicator);
	}

	DQSystem switchRef(DQSystem srcSystem) {
		return switchRef(seq.DQ_SYSTEM, new DQSystemDao(dest), srcSystem);
	}

	private <T extends RootEntity> T switchRef(int type, RootEntityDao<T, ?> dao, T srcEntity) {
		if (srcEntity == null)
			return null;
		long id = seq.get(type, srcEntity.getRefId());
		if (id == 0)
			return null;
		return dao.getForId(id);
	}

	Long getDestImpactMethodId(Long srcMethodId) {
		if (srcMethodId == null)
			return null;
		ImpactMethodDao srcDao = new ImpactMethodDao(source);
		ImpactMethodDescriptor srcDescriptor = srcDao
				.getDescriptor(srcMethodId);
		if (srcDescriptor == null)
			return null;
		return seq.get(seq.IMPACT_METHOD, srcDescriptor.getRefId());
	}

	Long getDestProcessId(Long srcProcessId) {
		if (srcProcessId == null)
			return null;
		ProcessDao srcDao = new ProcessDao(source);
		ProcessDescriptor srcDescriptor = srcDao.getDescriptor(srcProcessId);
		if (srcDescriptor == null)
			return null;
		return seq.get(seq.PROCESS, srcDescriptor.getRefId());
	}

}
