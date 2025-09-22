package org.openlca.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.openlca.core.database.DataPackages;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

import gnu.trove.set.hash.TLongHashSet;

public class ProviderReplacer {

	private final IDatabase db;
	private final TechIndex idx;
	private final DataPackages dataPackages;
	private boolean excludeDataPackageDatasets;

	private ProviderReplacer(IDatabase db) {
		this.db = db;
		this.idx = TechIndex.of(db);
		this.dataPackages = db.getDataPackages();
	}

	public static ProviderReplacer of(IDatabase db) {
		return new ProviderReplacer(db);
	}

	public ProviderReplacer excludeDataPackageDatasets() {
		this.excludeDataPackageDatasets = true;
		return this;
	}

	/// Returns the list of processes that are used as default providers
	/// in the database.
	public List<ProcessDescriptor> getUsedProviders() {
		if (db == null)
			return Collections.emptyList();

		var q = "select distinct f_default_provider from tbl_exchanges";
		var ids = new HashSet<Long>();
		NativeSql.on(db).query(q, r -> {
			var id = r.getLong(1);
			if (id != 0) {
				ids.add(id);
			}
			return true;
		});

		if (ids.isEmpty())
			return Collections.emptyList();

		var list = new ArrayList<ProcessDescriptor>(ids.size());
		for (var id : ids) {
			for (var p : idx.getProviders(id)) {
				if (p.provider() instanceof ProcessDescriptor process) {
					list.add(process);
					break;
				}
			}
		}

		return list;
	}

	public List<FlowDescriptor> getProviderFlowsOf(ProcessDescriptor p) {
		if (p == null)
			return Collections.emptyList();
		var flows = new ArrayList<FlowDescriptor>();
		for (var provider : idx.getProviders(p.id)) {
			if (provider.flow() != null && !flows.contains(provider.flow())) {
				flows.add(provider.flow());
			}
		}
		return flows;
	}

	public List<ProcessDescriptor> getProvidersOf(FlowDescriptor flow) {
		if (flow == null)
			return Collections.emptyList();
		var providers = new ArrayList<ProcessDescriptor>();
		for (var p : idx) {
			if (Objects.equals(p.flow(), flow)
					&& (p.provider() instanceof ProcessDescriptor process)
					&& !providers.contains(process)) {
				providers.add(process);
			}
		}
		return providers;
	}

	public void replace(
			ProcessDescriptor source, ProcessDescriptor target, FlowDescriptor flow) {
		if (source == null || target == null || flow == null)
			return;
		var sql = NativeSql.on(db);

		var skip = new TLongHashSet();
		var skipQ = "select id, data_package from tbl_processes " +
				"where data_package is not null";
		sql.query(skipQ, r -> {
			if (excludeDataPackageDatasets || dataPackages.isLibrary(r.getString(2))) {
				skip.add(r.getLong(1));
			}
			return true;
		});

		var changed = new TLongHashSet();
		var q = """
				select
				  f_owner,
					f_default_provider
				from tbl_exchanges where f_default_provider =\s"""
				+ source.id + " and f_flow = " + flow.id;
		sql.updateRows(q, r -> {
			var ownerId = r.getLong(1);
			if (skip.contains(ownerId))
				return true;
			changed.add(ownerId);
			r.updateLong(2, target.id);
			r.updateRow();
			return true;
		});

		if (changed.isEmpty())
			return;

		VersionUpdate.of(db, Process.class).run(changed);
	}
}
