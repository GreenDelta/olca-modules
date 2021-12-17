package org.openlca.io.ilcd.input;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ExchangeProviderQueue;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.io.DataStore;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowSync;
import org.openlca.util.Strings;

public class ImportConfig {

	private final DataStore store;
	private final IDatabase db;
	private final FlowSync flowSync;
	private final ImportLog log;

	private boolean allFlows;
	private String[] langOrder = {"en"};
	private ExchangeProviderQueue providers;
	private HashSet<ImpactMethodDescriptor> createdMethods;

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
	 * Returns the impact methods that were created during the import. As impact
	 * methods do not have UUIDs in ILCD (in fact "ILCD LCIA methods" are
	 * impact categories or indicators) we need to identify them by name. If an
	 * impact category is newly created in an import, and it has one or more
	 * LCIA method references (by name) we also create the LCIA methods then
	 * (also if there is a method with the same name already in openLCA).
	 */
	HashSet<ImpactMethodDescriptor> createdMethods() {
		return createdMethods;
	}
}
