package org.openlca.core.matrix.product.index;

import java.util.ArrayList;

import org.openlca.core.matrix.Provider;
import org.openlca.core.model.AllocationMethod;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

public class ProviderTable {

	private final TLongIntHashMap index = new TLongIntHashMap(
			Constants.DEFAULT_CAPACITY,
			Constants.DEFAULT_LOAD_FACTOR,
			0L, // no key
			-1); // no value

	private final ArrayList<Provider> providers = new ArrayList<>();

	/**
	 * Stores the default allocation methods of the processes: processID =>
	 * AllocationMethod.
	 * 
	 * TODO: we could store the default allocation method directly in the
	 * descriptor.
	 */
	private final TLongObjectHashMap<AllocationMethod> allocMap = new TLongObjectHashMap<>();

	public Provider get(long id, long flowId) {
		throw new RuntimeException(" - not implemented -");
	}

}
