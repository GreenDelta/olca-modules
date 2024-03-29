package org.openlca.io.olca;

import java.util.HashMap;
import java.util.List;

import org.openlca.core.database.ActorDao;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.EpdDao;
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
import org.openlca.core.database.RefEntityDao;
import org.openlca.core.database.ResultDao;
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
class Seq {

	private final Logger log = LoggerFactory.getLogger(getClass());

	static final int CATEGORY = 0;
	static final int LOCATION = 1;
	static final int ACTOR = 2;
	static final int SOURCE = 3;
	static final int UNIT = 4;
	static final int UNIT_GROUP = 5;
	static final int FLOW_PROPERTY = 6;
	static final int FLOW = 7;
	static final int CURRENCY = 8;
	static final int PROCESS = 9;
	static final int PRODUCT_SYSTEM = 10;
	static final int IMPACT_CATEGORY = 11;
	static final int IMPACT_METHOD = 12;
	static final int NW_SET = 13;
	static final int PROJECT = 14;
	static final int DQ_SYSTEM = 15;
	static final int SOCIAL_INDICATOR = 16;
	static final int RESULT = 17;
	static final int EPD = 18;

	private final HashMap<String, Long>[] sequences;

	@SuppressWarnings("unchecked")
	public Seq(IDatabase database) {
		sequences = new HashMap[19];
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
		index(RESULT, new ResultDao(db));
		index(EPD, new EpdDao(db));
	}

	private void index(int type, RefEntityDao<?, ?> dao) {
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
