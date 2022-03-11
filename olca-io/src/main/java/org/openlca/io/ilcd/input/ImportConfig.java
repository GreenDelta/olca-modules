package org.openlca.io.ilcd.input;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ExchangeProviderQueue;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.io.DataStore;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowSync;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;

public class ImportConfig {

	private final DataStore store;
	private final IDatabase db;
	private final FlowSync flowSync;
	private final ImportLog log;

	private boolean allFlows;
	private boolean withGabiGraphs = false;
	private String[] langOrder = {"en"};
	private ExchangeProviderQueue providers;
	private Map<String, ImpactMethodDescriptor> methods;
	private Map<String, Location> locations;

	public ImportConfig(DataStore store, IDatabase db) {
		this(store, db, null);
	}

	public ImportConfig(DataStore store, IDatabase db, FlowMap flowMap) {
		this.store = Objects.requireNonNull(store);
		this.db = Objects.requireNonNull(db);
		log = new ImportLog();
		flowSync = flowMap == null
			? FlowSync.of(db, FlowMap.empty())
			: FlowSync.of(db, flowMap);
		flowSync.withLog(log);
	}

	public ImportConfig withAllFlows(boolean b) {
		allFlows = b;
		return this;
	}

	/**
	 * Set if Gabi graphs are supported in eILCD model imports or not. Gabi has
	 * some specific model features: processes can be connected by different flows
	 * (e.g. a material can be connected with a transport flow); the same process
	 * can occur with different scaling factors in the same graph; processes can
	 * be connected by arbitrary flow types (not only wastes and products), and
	 * more. When Gabi graph support is enabled and an eILCD model of unknown
	 * origin is imported, copies of the processes in the system are created and
	 * linked in order to make it computable in openLCA.
	 */
	public ImportConfig withGabiGraphSupport(boolean b) {
		withGabiGraphs = b;
		return this;
	}

	public boolean hasGabiGraphSupport() {
		return withGabiGraphs;
	}

	/**
	 * Define the order in which a multi-language string should be evaluated. It
	 * first checks if there is a string for the first language of this list, then
	 * the second, etc.
	 */
	public ImportConfig withLanguageOrder(String... codes) {
		if (codes != null && codes.length > 0) {
			var filtered = Arrays.stream(codes)
				.filter(Strings::notEmpty)
				.map(s -> s.trim().toLowerCase())
				.toArray(String[]::new);
			if (filtered.length > 0) {
				langOrder = filtered;
			}
		}
		return this;
	}

	public ImportLog log() {
		return log;
	}

	public DataStore store() {
		return store;
	}

	public IDatabase db() {
		return db;
	}

	public boolean withAllFlows() {
		return allFlows;
	}

	String[] langOrder() {
		return langOrder;
	}

	public FlowSync flowSync() {
		return flowSync;
	}

	public ExchangeProviderQueue providers() {
		if (providers == null) {
			providers = ExchangeProviderQueue.create(db);
		}
		return providers;
	}

	String str(List<LangString> list) {
		return LangString.getFirst(list, langOrder);
	}

	/**
	 * Returns the impact method of the given name that was created during the
	 * import. As impact methods do not have UUIDs in ILCD (in fact "ILCD LCIA
	 * methods" are impact categories or indicators) we need to identify them by
	 * name. If an impact category is newly created in an import, and it has one
	 * or more LCIA method references (by name) we also create the LCIA methods
	 * then (also if there is a method with the same name already in openLCA).
	 */
	ImpactMethodDescriptor impactMethodOf(String name) {
		if (Strings.nullOrEmpty(name))
			return null;
		if (methods == null) {
			methods = new HashMap<>();
		}
		var key = name.trim().toLowerCase();
		var descriptor = methods.get(key);
		if (descriptor != null) {
			return descriptor;
		}
		var method = db.insert(ImpactMethod.of(name));
		log.imported(method);
		descriptor = Descriptor.of(method);
		methods.put(key, descriptor);
		return descriptor;
	}

	Location locationOf(String code) {
		if (Strings.nullOrEmpty(code))
			return null;
		if (locations == null) {
			locations = new HashMap<>();
			db.getAll(Location.class).forEach(
				loc -> locations.put(loc.code, loc));
		}
		var cached = locations.get(code);
		if (cached != null)
			return cached;
		var loc = new Location();
		loc.refId = KeyGen.get(code);
		loc.code = code;
		loc.name = code;
		db.insert(loc);
		log.imported(loc);
		locations.put(code, loc);
		return loc;
	}

	<T extends RootEntity> T insert(T e) {
		var r = db.insert(e);
		log.imported(r);
		return r;
	}
}
