package org.openlca.core.library;

import org.openlca.commons.Strings;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Version;

import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;

/// Removes data from product systems that became invalid when existing
/// database processes were turned into library processes. This happens when a
/// library is mounted on a database that already contains its data sets
/// without the library tag, see [MountAction#UPDATE] and [MountAction#RETAG].
///
/// A library process keeps only its quantitative reference in the database;
/// its other exchanges and its parameters are stored in the library. Thus, a
/// process link whose consuming process is a library process points to an
/// exchange that does not exist anymore. This cleanup:
///
/// 1. deletes all process links whose consuming process is a library process,
/// 2. removes the providers of these deleted links from the process lists of
///    their product systems, if they are not used anymore (neither as
///    reference process nor as provider of a remaining link),
/// 3. deletes parameter redefinitions of product system parameter sets that
///    have a library process or library impact category as context, and
/// 4. increments the version and updates the last-change timestamp of each
///    changed product system, so that the change is visible as a local
///    modification, e.g. in version control.
///
/// The cleanup is idempotent: running it on a clean database changes nothing.
public class LibraryLinkCleanup implements Runnable {

	private final IDatabase db;
	private final NativeSql sql;
	private final TLongHashSet changedSystems = new TLongHashSet();

	private LibraryLinkCleanup(IDatabase db) {
		this.db = db;
		this.sql = NativeSql.on(db);
	}

	public static LibraryLinkCleanup of(IDatabase db) {
		return new LibraryLinkCleanup(db);
	}

	/// Returns the IDs of the product systems that were changed by this
	/// cleanup.
	public TLongHashSet changedSystems() {
		return new TLongHashSet(changedSystems);
	}

	@Override
	public void run() {
		var libProcesses = libraryIdsOf("tbl_processes");
		var libImpacts = libraryIdsOf("tbl_impact_categories");
		if (libProcesses.isEmpty() && libImpacts.isEmpty())
			return;

		if (!libProcesses.isEmpty()) {
			var dropped = deleteLinksOf(libProcesses);
			pruneProcesses(dropped);
		}
		var contexts = new TLongHashSet(libProcesses);
		contexts.addAll(libImpacts);
		deleteRedefsOf(contexts);

		if (changedSystems.isEmpty())
			return;
		incVersions();
		db.clearCache();
	}

	private TLongHashSet libraryIdsOf(String table) {
		var ids = new TLongHashSet();
		sql.query("select id, library from " + table, r -> {
			if (Strings.isNotBlank(r.getString(2))) {
				ids.add(r.getLong(1));
			}
			return true;
		});
		return ids;
	}

	/// Deletes the process links with a library process as consumer and returns
	/// the providers of the deleted links per product system.
	private TLongObjectHashMap<TLongHashSet> deleteLinksOf(
		TLongHashSet libProcesses
	) {
		var dropped = new TLongObjectHashMap<TLongHashSet>();
		var query = "select f_product_system, f_provider, f_process "
			+ "from tbl_process_links";
		sql.updateRows(query, r -> {
			if (!libProcesses.contains(r.getLong(3)))
				return true;
			long systemId = r.getLong(1);
			var providers = dropped.get(systemId);
			if (providers == null) {
				providers = new TLongHashSet();
				dropped.put(systemId, providers);
			}
			providers.add(r.getLong(2));
			changedSystems.add(systemId);
			r.deleteRow();
			return true;
		});
		return dropped;
	}

	/// Removes the providers of deleted links from the process lists of their
	/// product systems when they are not used anymore. Other unlinked processes
	/// are kept, as they may have been added intentionally.
	private void pruneProcesses(TLongObjectHashMap<TLongHashSet> dropped) {
		if (dropped.isEmpty())
			return;

		// collect the processes that are still used in the changed systems
		var used = new TLongObjectHashMap<TLongHashSet>();
		NativeSql.QueryResultHandler collector = r -> {
			long systemId = r.getLong(1);
			if (!dropped.containsKey(systemId))
				return true;
			var ids = used.get(systemId);
			if (ids == null) {
				ids = new TLongHashSet();
				used.put(systemId, ids);
			}
			ids.add(r.getLong(2));
			return true;
		};
		sql.query(
			"select id, f_reference_process from tbl_product_systems", collector);
		sql.query(
			"select f_product_system, f_provider from tbl_process_links", collector);

		var query = "select f_product_system, f_process "
			+ "from tbl_product_system_processes";
		sql.updateRows(query, r -> {
			long systemId = r.getLong(1);
			var providers = dropped.get(systemId);
			if (providers == null)
				return true;
			long processId = r.getLong(2);
			if (!providers.contains(processId))
				return true;
			var usedIds = used.get(systemId);
			if (usedIds != null && usedIds.contains(processId))
				return true;
			r.deleteRow();
			return true;
		});
	}

	/// Deletes the parameter redefinitions of product system parameter sets
	/// that have one of the given library data sets as context. The parameters
	/// of library data sets are not stored in the database.
	private void deleteRedefsOf(TLongHashSet contexts) {
		if (contexts.isEmpty())
			return;

		// map the parameter sets to their product systems; redefinitions
		// of other owners (e.g. project variants) are not touched
		var setSystems = new TLongLongHashMap();
		sql.query(
			"select id, f_product_system from tbl_parameter_redef_sets", r -> {
				setSystems.put(r.getLong(1), r.getLong(2));
				return true;
			});
		if (setSystems.isEmpty())
			return;

		var query = "select f_owner, f_context from tbl_parameter_redefs";
		sql.updateRows(query, r -> {
			long ownerId = r.getLong(1);
			if (!setSystems.containsKey(ownerId))
				return true;
			if (!contexts.contains(r.getLong(2)))
				return true;
			changedSystems.add(setSystems.get(ownerId));
			r.deleteRow();
			return true;
		});
	}

	private void incVersions() {
		long now = System.currentTimeMillis();
		var query = "select id, version, last_change from tbl_product_systems";
		sql.updateRows(query, r -> {
			if (!changedSystems.contains(r.getLong(1)))
				return true;
			var version = new Version(r.getLong(2));
			version.incUpdate();
			r.updateLong(2, version.getValue());
			r.updateLong(3, now);
			r.updateRow();
			return true;
		});
	}
}
