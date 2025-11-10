package org.openlca.io.hestia;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.io.maps.FlowMap;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.io.UnitMappingEntry;
import org.openlca.io.maps.FlowSync;
import org.openlca.io.maps.SyncFlow;
import org.openlca.util.KeyGen;
import org.openlca.commons.Strings;

class FlowFetch {

	private final ImportLog log;
	private final IDatabase db;
	private final FlowSync sync;
	private final UnitMap units;

	private FlowFetch(
			ImportLog log, IDatabase db, FlowSync sync, UnitMap units
	) {
		this.log = log;
		this.db = db;
		this.sync = sync;
		this.units = units;
	}

	static FlowFetch of(ImportLog log, IDatabase db, FlowMap map) {
		var sync = FlowSync.of(db, map);
		var units = UnitMap.of(db);
		return new FlowFetch(log, db, sync, units);
	}

	SyncFlow get(Term term, Site site, FlowType defaultType) {
		if (term == null)
			return SyncFlow.empty();

		// first, try a regionalized mapping
		var termId = term.id();
		if (site != null) {
			var regId = termId + "/" + site.siteType();
			var f = sync.get(regId);
			if (f.isPresent())
				return f;
		}

		// try mapped or cached
		var f = sync.get(termId);
		if (f.isPresent())
			return f;

		var u = units.get(term.unit());
		if (u == null) {
			log.error("could not map flow " + termId
					+ " because it's unit is unmapped: " + term.unit());
			return SyncFlow.empty();
		}

		var refId = KeyGen.get("hestia-flow", termId);
		var flow = db.get(Flow.class, refId);
		if (flow != null)
			return syncFlowOf(termId, flow, u);

		flow = Flow.of(term.name(), defaultType, u.flowProperty);
		flow.refId = refId;
		flow.description = "A HESTIA flow: " +
				"https://www.hestia.earth/term/" + termId;
		var cat = term.getCategoryName();
		if (Strings.isNotBlank(cat)) {
			flow.category = CategoryDao.sync(db, ModelType.FLOW, cat);
		}
		db.insert(flow);
		log.imported(flow);
		return syncFlowOf(termId, flow, u);
	}

	private SyncFlow syncFlowOf(String termId, Flow flow, UnitMappingEntry u) {
		if (flow == null || u == null)
			return SyncFlow.empty();
		var f = new SyncFlow(
				flow, flow.getFactor(u.flowProperty), u.unit, null, false, 1.0);
		sync.put(termId, f);
		return f;
	}

}
