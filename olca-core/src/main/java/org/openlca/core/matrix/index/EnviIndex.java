package org.openlca.core.matrix.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.cache.FlowTable;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * The row index $\mathit{Idx}_B$ of the intervention matrix $\mathbf{B}$. It
 * maps the (elementary) flows $\mathit{F}$ of the processes in the product
 * system to the $k$ rows of $\mathbf{B}$.
 * <p>
 * $$\mathit{Idx}_B: \mathit{F} \mapsto [0 \dots k-1]$$
 */
public abstract class EnviIndex implements MatrixIndex<EnviFlow> {

	protected final ArrayList<EnviFlow> flows = new ArrayList<>();

	private EnviIndex() {
	}

	/**
	 * Creates an empty flow index.
	 */
	public static EnviIndex create() {
		return new NormalFlowIndex();
	}

	/**
	 * Creates a flow index and fills it with the flows that are used in the
	 * given impacts.
	 */
	public static EnviIndex create(IDatabase db, ImpactIndex impacts) {
		var index = create();
		if (db == null || impacts == null)
			return index;

		// collect flows and IDs
		var flows = new FlowDao(db).descriptorMap();
		var directions = FlowTable.directionsOf(
			db, flows.valueCollection());

		// scan the factor table
		var sql = "select f_impact_category, f_flow " +
							"from tbl_impact_factors";
		NativeSql.on(db).query(sql, r -> {
			var impact = r.getLong(1);
			if (!impacts.contains(impact))
				return true;
			var flowID = r.getLong(2);
			var flow = flows.get(flowID);
			if (flow == null)
				return true;
			if (directions.get(flowID) < 0) {
				index.add(EnviFlow.inputOf(flow));
			} else {
				index.add(EnviFlow.outputOf(flow));
			}
			return true;
		});
		return index;
	}

	/**
	 * Creates an empty regionalized flow index.
	 */
	public static EnviIndex createRegionalized() {
		return new RegionalizedFlowIndex();
	}

	/**
	 * Creates a regionalized flow index and fills it with the flows
	 * that are used in the given impacts.
	 */
	public static EnviIndex createRegionalized(
		IDatabase db, ImpactIndex impacts) {
		var index = createRegionalized();
		if (db == null || impacts == null)
			return index;

		// collect flows, locations and IDs
		var flows = new FlowDao(db).descriptorMap();
		var directions = FlowTable.directionsOf(
			db, flows.valueCollection());
		var locations = new LocationDao(db).descriptorMap();

		// scan the factor table
		var sql = "select f_impact_category, f_flow, f_location " +
							"from tbl_impact_factors";
		NativeSql.on(db).query(sql, r -> {
			var impact = r.getLong(1);
			if (!impacts.contains(impact))
				return true;
			var flowID = r.getLong(2);
			var flow = flows.get(flowID);
			if (flow == null)
				return true;
			var locationID = r.getLong(3);
			var location = locationID != 0
				? locations.get(locationID)
				: null;
			if (directions.get(flowID) < 0) {
				index.add(EnviFlow.inputOf(flow, location));
			} else {
				index.add(EnviFlow.outputOf(flow, location));
			}
			return true;
		});
		return index;
	}

	/**
	 * Returns true if the given index is null or empty.
	 */
	public static boolean isEmpty(EnviIndex idx) {
		return idx == null || idx.size() == 0;
	}

	public abstract boolean isRegionalized();

	@Override
	public final int size() {
		return flows.size();
	}

	@Override
	public final boolean isEmpty() {
		return flows.isEmpty();
	}

	@Override
	public final EnviFlow at(int i) {
		return flows.get(i);
	}

	@Override
	public final int of(EnviFlow flow) {
		if (flow == null)
			return -1;
		return of(flow.flow(), flow.location());
	}

	public abstract int of(FlowDescriptor flow);

	public abstract int of(FlowDescriptor flow, LocationDescriptor loc);

	public abstract int of(long flowID);

	public abstract int of(long flowID, long locationID);

	public abstract boolean isInput(long flowID);

	public abstract boolean isInput(long flowID, long locationID);

	@Override
	public final boolean contains(EnviFlow flow) {
		return of(flow) >= 0;
	}

	public final boolean contains(long flowID) {
		return of(flowID) >= 0;
	}

	public final boolean contains(long flowID, long locationID) {
		return of(flowID, locationID) >= 0;
	}

	/**
	 * This method should be only called from inventory builders to index flows.
	 * Only when this index is not regionalized, it is save to pass a null value for
	 * the locations into this method.
	 */
	public final int register(
		TechFlow product,
		CalcExchange e,
		FlowTable flows,
		TLongObjectHashMap<LocationDescriptor> locations) {

		int idx = of(e.flowId, e.locationId);
		if (idx >= 0)
			return idx;
		var flow = flows.get(e.flowId);
		if (flow == null)
			return -1;

		if (!isRegionalized()) {
			return e.isInput
				? add(EnviFlow.inputOf(flow))
				: add(EnviFlow.outputOf(flow));
		}

		// Take the location from the exchange. If the exchange does not have a
		// location, then take it from the process.
		LocationDescriptor loc = null;
		if (e.locationId > 0) {
			loc = locations.get(e.locationId);
		}
		if (loc == null) {
			if (product.provider() instanceof ProcessDescriptor d) {
				if (d.location != null) {
					loc = locations.get(d.location);
				}
			}
		}
		return e.isInput
			? add(EnviFlow.inputOf(flow, loc))
			: add(EnviFlow.outputOf(flow, loc));
	}

	@Override
	public final void each(IndexConsumer<EnviFlow> fn) {
		if (fn == null)
			return;
		for (int i = 0; i < flows.size(); i++) {
			fn.accept(i, flows.get(i));
		}
	}

	@Override
	public final Iterator<EnviFlow> iterator() {
		return Collections.unmodifiableList(flows).iterator();
	}

	/**
	 * Creates a new set with the flows of this index.
	 */
	@Override
	public final Set<EnviFlow> content() {
		return new HashSet<>(flows);
	}

	@Override
	public abstract EnviIndex copy();

	private static class NormalFlowIndex extends EnviIndex {

		private final TLongIntHashMap index;

		private NormalFlowIndex() {
			index = new TLongIntHashMap(
				Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR,
				-1L, // no entry key
				-1); // no entry value
		}

		@Override
		public boolean isRegionalized() {
			return false;
		}

		@Override
		public int of(FlowDescriptor flow) {
			return flow == null
				? -1
				: index.get(flow.id);
		}

		@Override
		public int of(FlowDescriptor flow, LocationDescriptor _loc) {
			return of(flow);
		}

		@Override
		public int of(long flowID) {
			return index.get(flowID);
		}

		@Override
		public int of(long flowID, long _locID) {
			return index.get(flowID);
		}

		@Override
		public int add(EnviFlow f) {
			if (f == null)
				return -1;
			var pos = of(f);
			if (pos >= 0)
				return pos;
			var idx = flows.size();
			flows.add(f);
			index.put(f.flowId(), idx);
			return idx;
		}

		@Override
		public boolean isInput(long flowID) {
			var idx = index.get(flowID);
			return idx >= 0 && flows.get(idx).isInput();
		}

		@Override
		public boolean isInput(long flowID, long _locID) {
			return isInput(flowID);
		}

		@Override
		public NormalFlowIndex copy() {
			var copy = new NormalFlowIndex();
			copy.index.putAll(this.index);
			copy.flows.addAll(this.flows);
			return copy;
		}
	}

	private static class RegionalizedFlowIndex extends EnviIndex {

		private final HashMap<LongPair, Integer> index = new HashMap<>();

		@Override
		public boolean isRegionalized() {
			return true;
		}

		@Override
		public int of(FlowDescriptor flow) {
			return flow == null
				? -1
				: of(flow.id, 0L);
		}

		@Override
		public int of(FlowDescriptor flow, LocationDescriptor loc) {
			if (flow == null)
				return -1;
			return loc == null
				? of(flow.id, 0L)
				: of(flow.id, loc.id);
		}

		@Override
		public int of(long flowID) {
			return of(flowID, 0L);
		}

		@Override
		public int of(long flowID, long locationID) {
			var pair = LongPair.of(flowID, locationID);
			var idx = index.get(pair);
			return idx == null ? -1 : idx;
		}

		@Override
		public int add(EnviFlow f) {
			if (f == null)
				return -1;
			var pos = of(f);
			if (pos >= 0)
				return pos;
			var idx = flows.size();
			flows.add(f);
			index.put(f.regionalizedId(), idx);
			return idx;
		}

		@Override
		public boolean isInput(long flowID) {
			return isInput(flowID, 0L);
		}

		@Override
		public boolean isInput(long flowID, long locationID) {
			var idx = index.get(LongPair.of(flowID, locationID));
			return idx != null && flows.get(idx).isInput();
		}

		@Override
		public RegionalizedFlowIndex copy() {
			var copy = new RegionalizedFlowIndex();
			copy.flows.addAll(this.flows);
			copy.index.putAll(this.index);
			return copy;
		}
	}
}
