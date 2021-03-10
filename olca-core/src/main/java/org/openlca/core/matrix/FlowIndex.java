package org.openlca.core.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.NativeSql;
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
public final class FlowIndex implements MatrixIndex<IndexFlow> {

	private final TLongIntHashMap index;
	private final HashMap<LongPair, Integer> regIndex;
	private final ArrayList<IndexFlow> flows = new ArrayList<>();

	private FlowIndex(boolean isRegionalized) {
		if (isRegionalized) {
			index = null;
			regIndex = new HashMap<>();
		} else {
			index = new TLongIntHashMap(
				Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR,
				-1L, // no entry key
				-1); // no entry value
			regIndex = null;
		}
	}

	/**
	 * Creates an empty flow index.
	 */
	public static FlowIndex create() {
		return new FlowIndex(false);
	}

	/**
	 * Creates a flow index and fills it with the flows that are used in the
	 * given impacts.
	 */
	public static FlowIndex create(IDatabase db, ImpactIndex impacts) {
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
				index.add(IndexFlow.inputOf(flow));
			} else {
				index.add(IndexFlow.outputOf(flow));
			}
			return true;
		});
		return index;
	}

	/**
	 * Creates an empty regionalized flow index.
	 */
	public static FlowIndex createRegionalized() {
		return new FlowIndex(true);
	}

	/**
	 * Creates a regionalized flow index and fills it with the flows
	 * that are used in the given impacts.
	 */
	public static FlowIndex createRegionalized(
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
				index.add(IndexFlow.inputOf(flow, location));
			} else {
				index.add(IndexFlow.outputOf(flow, location));
			}
			return true;
		});
		return index;
	}

	/**
	 * Returns true if the given index is null or empty.
	 */
	public static boolean isEmpty(FlowIndex idx) {
		return idx == null || idx.size() == 0;
	}

	public boolean isRegionalized() {
		return regIndex != null;
	}

	@Override
	public int size() {
		return flows.size();
	}

	@Override
	public boolean isEmpty() {
		return flows.isEmpty();
	}

	@Override
	public IndexFlow at(int i) {
		if (i < 0 || i >= flows.size())
			return null;
		return flows.get(i);
	}

	@Override
	public int of(IndexFlow flow) {
		if (flow == null)
			return -1;
		return of(flow.flow, flow.location);
	}

	public int of(FlowDescriptor flow) {
		if (flow == null)
			return -1;
		return index != null
			? index.get(flow.id)
			: of(flow.id, 0L);
	}

	public int of(FlowDescriptor flow, LocationDescriptor loc) {
		if (flow == null)
			return -1;
		return index != null
			? index.get(flow.id)
			: of(flow.id, loc != null ? loc.id : 0L);
	}

	public int of(long flowID) {
		return index != null
			? index.get(flowID)
			: of(flowID, 0L);
	}

	public int of(long flowID, long locationID) {
		if (regIndex != null) {
			var idx = regIndex.get(LongPair.of(flowID, locationID));
			return idx == null ? -1 : idx;
		}
		return index == null
			? -1
			: index.get(flowID);
	}

	@Override
	public boolean contains(IndexFlow flow) {
		return of(flow) >= 0;
	}

	public boolean contains(long flowID) {
		return of(flowID) >= 0;
	}

	public boolean contains(long flowID, long locationID) {
		return of(flowID, locationID) >= 0;
	}

	@Override
	public int add(IndexFlow elem) {
		if (elem == null)
			return -1;
		var pos = of(elem);
		if (pos >= 0)
			return pos;
		var idx = flows.size();
		flows.add(elem);
		if (regIndex != null) {
			regIndex.put(elem.regionalizedId(), idx);
		} else if (index != null) {
			index.put(elem.id(), idx);
		}
		return idx;
	}

	/**
	 * This method should be only called from inventory builders to index flows.
	 * Only when this index is not regionalized, it is save to pass a null value for
	 * the locations into this method.
	 */
	public int register(
		ProcessProduct product,
		CalcExchange e,
		FlowTable flows,
		TLongObjectHashMap<LocationDescriptor> locations) {

		int i = regIndex != null
			? of(e.flowId, e.locationId)
			: of(e.flowId);
		if (i >= 0)
			return i;
		var flow = flows.get(e.flowId);
		if (flow == null)
			return -1;

		if (regIndex == null) {
			return e.isInput
				? add(IndexFlow.inputOf(flow))
				: add(IndexFlow.outputOf(flow));
		}

		// take the location from the exchange
		// if the exchange does not have a location
		// the take it from the flow.
		LocationDescriptor loc = null;
		if (e.locationId > 0) {
			loc = locations.get(e.locationId);
		}
		if (loc == null) {
			if (product.process instanceof ProcessDescriptor) {
				var d = (ProcessDescriptor) product.process;
				if (d.location != null) {
					loc = locations.get(d.location);
				}
			}
		}
		return e.isInput
			? add(IndexFlow.inputOf(flow, loc))
			: add(IndexFlow.outputOf(flow, loc));
	}

	@Override
	public void each(IndexConsumer<IndexFlow> fn) {
		if (fn == null)
			return;
		for (int i = 0; i < flows.size(); i++) {
			fn.accept(i, flows.get(i));
		}
	}

	/**
	 * Creates a new set with the flows of this index.
	 */
	public Set<IndexFlow> flows() {
		return new HashSet<>(flows);
	}

	public boolean isInput(long flowID) {
		if (regIndex != null)
			return isInput(flowID, 0L);
		if (index == null)
			return false;
		int i = index.get(flowID);
		if (i < 0)
			return false;
		var flow = flows.get(i);
		return flow.isInput;
	}

	public boolean isInput(long flowID, long locationID) {
		if (regIndex == null)
			return isInput(flowID);
		var key = LongPair.of(flowID, locationID);
		var i = regIndex.get(key);
		if (i == null)
			return false;
		var flow = flows.get(i);
		return flow.isInput;
	}

	@Override
	public FlowIndex copy() {

		// copy a regionalized index
		if (isRegionalized()) {
			var copy = createRegionalized();
			copy.flows.addAll(flows);
			var regIndex = Objects.requireNonNull(copy.regIndex);
			each((i, iFlow) -> {
				var locID = iFlow.location != null
					? iFlow.location.id
					: 0L;
				regIndex.put(LongPair.of(iFlow.flow.id, locID), i);
			});
			return copy;
		}

		// copy a non-regionalized index
		var copy = create();
		copy.flows.addAll(flows);
		var index = Objects.requireNonNull(copy.index);
		each((i, iFlow) -> index.put(iFlow.flow.id, i));
		return copy;
	}
}
