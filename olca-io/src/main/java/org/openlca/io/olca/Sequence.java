package org.openlca.io.olca;

import java.util.HashMap;
import java.util.List;

import org.openlca.core.database.ActorDao;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.NwSetDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.database.SocialIndicatorDao;
import org.openlca.core.database.SourceDao;
import org.openlca.core.database.UnitDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.descriptors.Descriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the mappings between reference IDs (UUID) and generated IDs (long) of
 * (imported) data sets. An instance of this class is created with the target
 * database in the import and initialized with the IDs of the data sets that are
 * already contained in this database.
 */
class Sequence {

	private Logger log = LoggerFactory.getLogger(getClass());

	int CATEGORY = 0;
	int LOCATION = 1;
	int ACTOR = 2;
	int SOURCE = 3;
	int UNIT = 4;
	int UNIT_GROUP = 5;
	int FLOW_PROPERTY = 6;
	int FLOW = 7;
	int CURRENCY = 8;
	int PROCESS = 9;
	int PRODUCT_SYSTEM = 10;
	int IMPACT_CATEGORY = 11;
	int IMPACT_METHOD = 12;
	int NW_SET = 13;
	int PROJECT = 14;
	int DQ_SYSTEM = 15;
	int SOCIAL_INDICATOR = 16;

	private final HashMap<String, Long>[] sequences;

	@SuppressWarnings("unchecked")
	public Sequence(IDatabase database) {
		sequences = new HashMap[17];
		for (int i = 0; i < sequences.length; i++)
			sequences[i] = new HashMap<>();
		init(database);
	}

	private void init(IDatabase db) {
		index(CATEGORY, new CategoryDao(db));
		index(LOCATION, new LocationDao(db));
		index(ACTOR, new ActorDao(db));
		index(SOURCE, new SourceDao(db));
		index(UNIT, new UnitDao(db));
		index(UNIT_GROUP, new UnitGroupDao(db));
		index(FLOW_PROPERTY, new FlowPropertyDao(db));
		index(FLOW, new FlowDao(db));
		index(CURRENCY, new CurrencyDao(db));
		index(PROCESS, new ProcessDao(db));
		index(PRODUCT_SYSTEM, new ProductSystemDao(db));
		index(IMPACT_CATEGORY, new ImpactCategoryDao(db));
		index(IMPACT_METHOD, new ImpactMethodDao(db));
		index(NW_SET, new NwSetDao(db));
		index(PROJECT, new ProjectDao(db));
		index(DQ_SYSTEM, new DQSystemDao(db));
		index(SOCIAL_INDICATOR, new SocialIndicatorDao(db));
	}

	private void index(int type, RootEntityDao<?, ?> dao) {
		List<? extends Descriptor> descriptors = dao.getDescriptors();
		for (Descriptor d : descriptors) {
			if (d.refId == null) {
				log.warn("found root entity without reference ID: {}", d);
				continue;
			}
			put(type, d.refId, d.id);
		}
	}

	public void put(int type, String refId, long genId) {
		if (refId == null)
			return;
		sequences[type].put(refId, genId);
	}

	public long get(int type, String refId) {
		if (refId == null)
			return 0;
		Long val = sequences[type].get(refId);
		return val == null ? 0 : val;
	}

	public boolean contains(int type, String refId) {
		return get(type, refId) != 0;
	}
}
