package org.openlca.core.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.openlca.core.database.IDatabase;
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
 *
 * $$\mathit{Idx}_B: \mathit{F} \mapsto [0 \dots k-1]$$
 */
public final class FlowIndex {

	public final boolean isRegionalized;
	private final TLongIntHashMap index;
	private final HashMap<LongPair, Integer> regIndex;
	private final ArrayList<IndexFlow> flows = new ArrayList<>();

	private FlowIndex(boolean isRegionalized) {
		this.isRegionalized = isRegionalized;
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

		return index;
	}

	/**
	 * Creates an empty regionalized flow index.
	 */
	public static FlowIndex createRegionalized() {
		return new FlowIndex(true);
	}

	/**
	 * Returns true if the given index is null or empty.
	 */
	public static boolean isEmpty(FlowIndex idx) {
		return idx == null || idx.size() == 0;
	}

	public int size() {
		return flows.size();
	}

	public boolean isEmpty() {
		return flows.isEmpty();
	}

	public IndexFlow at(int i) {
		if (i < 0 || i >= flows.size())
			return null;
		return flows.get(i);
	}

	public int of(IndexFlow flow) {
		if (flow == null)
			return -1;
		return of(flow.flow, flow.location);
	}

	public int of(FlowDescriptor flow) {
		if (flow == null)
			return -1;
		if (isRegionalized)
			return of(flow.id, 0L);
		return index.get(flow.id);
	}

	public int of(FlowDescriptor flow, LocationDescriptor loc) {
		if (flow == null)
			return -1;
		if (isRegionalized)
			return of(flow.id, loc != null ? loc.id : 0L);
		return index.get(flow.id);
	}

	public int of(long flowID) {
		if (isRegionalized)
			return of(flowID, 0L);
		return index.get(flowID);
	}

	public int of(long flowID, long locationID) {
		if (isRegionalized) {
			Integer idx = regIndex.get(LongPair.of(flowID, locationID));
			return idx == null ? -1 : idx;
		}
		return index.get(flowID);
	}

	public boolean contains(IndexFlow flow) {
		return of(flow) >= 0;
	}

	public boolean contains(FlowDescriptor flow) {
		return of(flow) >= 0;
	}

	public boolean contains(FlowDescriptor flow, LocationDescriptor location) {
		return of(flow, location) >= 0;
	}

	public boolean contains(long flowID) {
		return of(flowID) >= 0;
	}

	public boolean contains(long flowID, long locationID) {
		return of(flowID, locationID) >= 0;
	}

	/**
	 * Adds all flows of the given index to this index.
	 */
	public void putAll(FlowIndex other) {
		if (other == null || other == this)
			return;
		other.each((i, f) -> {
			if (contains(f))
				return;
			if (isRegionalized) {
				if (f.isInput) {
					putInput(f.flow, f.location);
				} else {
					putOutput(f.flow, f.location);
				}
			} else {
				if (f.isInput) {
					putInput(f.flow);
				} else {
					putOutput(f.flow);
				}
			}
		});
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

		int i = isRegionalized
				? of(e.flowId, e.locationId)
				: of(e.flowId);
		if (i >= 0)
			return i;
		var flow = flows.get(e.flowId);
		if (flow == null)
			return -1;

		if (!isRegionalized) {
			return e.isInput
					? putInput(flow)
					: putOutput(flow);
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
				? putInput(flow, loc)
				: putOutput(flow, loc);
	}

	public int putInput(FlowDescriptor flow) {
		return put(flow, null, true);
	}

	public int putInput(FlowDescriptor flow, LocationDescriptor location) {
		return put(flow, location, true);
	}

	public int putOutput(FlowDescriptor flow) {
		return put(flow, null, false);
	}

	public int putOutput(FlowDescriptor flow, LocationDescriptor location) {
		return put(flow, location, false);
	}

	private int put(FlowDescriptor flow,
			LocationDescriptor location,
			boolean isInput) {
		if (flow == null)
			return -1;

		int idx = flows.size();

		// check if the flow should be added
		if (isRegionalized) {
			long locID = location == null ? 0L : location.id;
			LongPair p = LongPair.of(flow.id, locID);
			Integer i = regIndex.get(p);
			if (i != null)
				return i;
			regIndex.put(p, idx);
		} else {
			int i = index.get(flow.id);
			if (i > -1)
				return i;
			index.put(flow.id, idx);
		}

		// create and add the index flow
		IndexFlow f = new IndexFlow();
		// f.index = idx;
		f.flow = flow;
		f.location = isRegionalized ? location : null;
		f.isInput = isInput;
		flows.add(f);
		return idx;
	}

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
		if (isRegionalized)
			return isInput(flowID, 0L);
		int i = index.get(flowID);
		if (i < 0)
			return false;
		IndexFlow flow = flows.get(i);
		return flow.isInput;
	}

	public boolean isInput(long flowID, long locationID) {
		if (!isRegionalized)
			return isInput(flowID);
		LongPair key = LongPair.of(flowID, locationID);
		Integer i = regIndex.get(key);
		if (i == null)
			return false;
		IndexFlow flow = flows.get(i);
		return flow.isInput;
	}

}
