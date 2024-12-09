package org.openlca.util;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.TLongLongMap;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.set.hash.TLongHashSet;

public class FlowReplacer {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final IDatabase db;
	private final Set<ModelType> ownerTypes;

	private FlowReplacer(IDatabase db) {
		this.db = Objects.requireNonNull(db);
		this.ownerTypes = EnumSet.noneOf(ModelType.class);
	}

	public static FlowReplacer of(IDatabase db) {
		return new FlowReplacer(db);
	}

	// region utils

	/// Get the used flows from the database. This includes flows from processes,
	/// impact categories, results, and EPDs. Note that maybe not all returned
	/// flows can be replaced, because they are maybe only used in library data.
	public static List<FlowDescriptor> getUsedFlowsOf(IDatabase db) {
		if (db == null)
			return Collections.emptyList();

		var tables = new String[]{
				"tbl_exchanges",
				"tbl_impact_factors",
				"tbl_flow_results",
				"tbl_epds"
		};
		var ids = new HashSet<Long>();
		for (var table : tables) {
			var q = "select distinct f_flow from " + table;
			NativeSql.on(db).query(q, r -> {
				ids.add(r.getLong(1));
				return true;
			});
		}

		return !ids.isEmpty()
				? new FlowDao(db).getDescriptors(ids)
				: Collections.emptyList();
	}

	/// Returns a list of flows with which the given flow could be replaced, in
	/// principle. This is the case when the flows have the same type and a
	/// common flow property. We further restrict the second condition so that
	/// the flows have exactly the same set of flow properties as the replacer
	/// is currently not smart enough to handle all conversions.
	public static List<FlowDescriptor> getCandidatesOf(
			IDatabase db, FlowDescriptor flow
	) {
		if (db == null || flow == null || flow.flowType == null)
			return Collections.emptyList();

		var sql = NativeSql.on(db);

		// a flow is a candidate of f if it has all flow properties
		// of f. We check this with simple check-sum.
		var props = new HashMap<Long, Integer>();
		var checkRef = new AtomicInteger(0);
		var propQ = "select distinct f_flow_property from " +
				"tbl_flow_property_factors where f_flow = " + flow.id;
		sql.query(propQ, r -> {
			props.put(r.getLong(1), checkRef.incrementAndGet());
			return true;
		});
		int checkSum = checkRef.get();
		if (props.isEmpty())
			return Collections.emptyList();

		var sums = new HashMap<Long, Integer>();
		var candidates = new HashSet<Long>();
		var candQ = """
				select distinct flow.id, fac.f_flow_property
				  from tbl_flows flow
				  inner join tbl_flow_property_factors fac
				  on flow.id = fac.f_flow
				  where flow.flow_type = '"""
				+ flow.flowType.name() + "'";
		sql.query(candQ, r -> {
			var propId = r.getLong(2);
			var i = props.get(propId);
			if (i == null)
				return true;
			var flowId = r.getLong(1);
			if (flowId == flow.id)
				return true;
			var sum = sums.compute(flowId, ($, old) -> old == null ? i : old + i);
			if (sum == checkSum) {
				candidates.add(flowId);
			}
			return true;
		});

		return candidates.isEmpty()
				? Collections.emptyList()
				: new FlowDao(db).getDescriptors(candidates);
	}

	// endregion

	// region config

	public FlowReplacer replaceIn(ModelType type) {
		if (type != null) {
			ownerTypes.add(type);
		}
		return this;
	}

	public FlowReplacer replaceIn(ModelType type, ModelType... more) {
		replaceIn(type);
		if (more != null) {
			for (var t : more) {
				replaceIn(t);
			}
		}
		return this;
	}

	// endregion

	public void replace(FlowDescriptor origin, FlowDescriptor target) {
		var def = RepDef.of(db, origin, target);
		if (def.err != null) {
			log.error("cannot replace flow: {}", def.err);
			return;
		}

		if (ownerTypes.contains(ModelType.PROCESS)) {
			replaceInProcesses(def);
		}

		if (ownerTypes.contains(ModelType.IMPACT_CATEGORY)
				|| ownerTypes.contains(ModelType.IMPACT_METHOD)) {
			replaceInImpactFactors(def);
		}

		// TODO: EPDs & results not yet implemented
	}

	private void replaceInProcesses(RepDef def) {

		// collect library processes, they are not changed
		var sql = NativeSql.on(db);
		var libQ = "select id, library from tbl_processes " +
				"where library is not null";
		var libProcs = new TLongHashSet();
		sql.query(libQ, r -> {
			var lib = r.getString(2);
			if (Strings.notEmpty(lib)) {
				libProcs.add(r.getLong(1));
			}
			return true;
		});

		// replace in exchanges
		var changed = new HashSet<Long>();
		var q = """
				select
				  f_owner,
				  f_flow,
				  f_flow_property_factor,
				  f_default_provider
				from tbl_exchanges where f_flow =\s""" + def.origin;
		NativeSql.on(db).updateRows(q, r -> {
			long procId = r.getLong(1);
			if (libProcs.contains(procId))
				return true;
			changed.add(procId);

			// change flow & flow property
			r.updateLong(2, def.target);
			r.updateLong(3, def.propFacs.get(r.getLong(3)));

			// if a provider is set, clear it
			long provider = r.getLong(4);
			if (provider != 0L) {
				r.updateLong(4, 0L);
			}
			r.updateRow();
			return true;
		});

		if (changed.isEmpty()) {
			log.info("no processes changed");
			return;
		}

		// replace in allocation factors
		var allocQ = """
				select f_process, f_product
				  from tbl_allocation_factors
				  where f_product =\s""" + def.origin;
		sql.updateRows(allocQ, r -> {
			var procId = r.getLong(1);
			if (!changed.contains(procId))
				return true;
			r.updateLong(2, def.target);
			return true;
		});

		// TODO: target flow property factors of product systems!

		// change in process links
		// TODO: it is a problem when the flow was replaced in one process but
		// not in the other because it was a library process. In such cases
		// the flow cannot be replaced, but this is currently not checked, on
		// option would be to exclude library flows from replacement

		var changedSystems = new HashSet<Long>();
		var linkQ = """
				select
				  f_product_system,
				  f_provider,
				  f_flow,
				  f_process
				  from tbl_process_links where f_flow =\s""" + def.origin;
		sql.updateRows(linkQ, r -> {
			long provider = r.getLong(2);
			long process = r.getLong(4);
			int state = 0;
			if (changed.contains(provider)) {
				state += 1;
			}
			if (changed.contains(process)) {
				state += 2;
			}

			if (state == 1) {
				log.error("could not update process link; " +
								"provider {} changed but process {} did not",
						provider, process);
			} else if (state == 2) {
				log.error("could not update process link; " +
								"process {} changed but provider {} did not",
						process, provider);
			} else if (state == 3) {
				changedSystems.add(r.getLong(1));
				r.updateLong(3, def.target);
				r.updateRow();
			}
			return true;
		});

		// TODO: increment versions

	}

	private void replaceInImpactFactors(RepDef def) {

		// LCIA categories from libraries do not contain
		// characterization factors in the database, so
		// there is no need to filter them explicitly
		var changed = new HashSet<Long>();
		var q = """
				select
				  f_impact_category,
				  f_flow,
				  f_flow_property_factor
				from tbl_impact_factors where f_flow = \s""" + def.origin;
		NativeSql.on(db).updateRows(q, r -> {
			changed.add(r.getLong(1));
			r.updateLong(2, def.target);
			r.updateLong(3, def.propFacs.get(r.getLong(3)));
			r.updateRow();
			return true;
		});

		// TODO: increment versions

	}


	private record RepDef(
			long origin, long target, TLongLongMap propFacs, String err
	) {

		static RepDef error(String msg) {
			return new RepDef(-1L, -1L, null, msg);
		}

		static RepDef of(
				IDatabase db, FlowDescriptor origin, FlowDescriptor target
		) {

			if (origin == null || target == null)
				return RepDef.error("original or target flow missing");

			var o = db.get(Flow.class, origin.id);
			var t = db.get(Flow.class, target.id);
			if (o == null || t == null)
				return RepDef.error("failed to load original or target flow");

			// map flow property factors with same flow properties; units of
			// the amounts can then stay the same; it is the current
			// pre-condition of the replacer that the target flow has at
			// least all flow properties of the original flow
			var propFacs = new TLongLongHashMap();
			for (var oFac : o.flowPropertyFactors) {
				FlowPropertyFactor tFac = null;
				for (var tf : t.flowPropertyFactors) {
					if (Objects.equals(oFac.flowProperty, tf.flowProperty)) {
						tFac = tf;
						break;
					}
				}
				if (tFac == null) {
					return RepDef.error(
							"no matching flow property in target flow: " + oFac.flowProperty);
				}
				propFacs.put(oFac.id, tFac.id);
			}

			return new RepDef(o.id, t.id, propFacs, null);
		}
	}
}
