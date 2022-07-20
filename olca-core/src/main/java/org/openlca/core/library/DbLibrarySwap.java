package org.openlca.core.library;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.jsonld.ZipStore;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;

/**
 * Tries to replace a background system of a database with a library.
 */
public class DbLibrarySwap implements Runnable {

	private final IDatabase db;
	private final Library library;

	public DbLibrarySwap(IDatabase db, Library library) {
		this.db = db;
		this.library = library;
	}

	@Override
	public void run() {
		try {
			var replacedTechFlows = techFlowsOf(db);
			var libId = library.name();
			var meta = new File(library.folder(), "meta.zip");
			try (var store = ZipStore.open(meta)) {
				var imp = new JsonImport(store, db);
				imp.setUpdateMode(UpdateMode.ALWAYS);
				imp.setCallback(e -> {
					if (e instanceof RootEntity ce) {
						ce.library = libId;
						db.update(ce);
					}
				});
				imp.run();
			}

			var replacedProcesses = new TLongHashSet();
			for (var techFlow : replacedTechFlows) {
				replacedProcesses.add(techFlow.providerId());
			}

			db.clearCache();
			var nativeSql = NativeSql.on(db);

			// remove process links to library processes
			var sql = "select f_process from tbl_process_links";
			nativeSql.updateRows(sql, r -> {
				var processId = r.getLong(1);
				if (replacedProcesses.contains(processId)) {
					r.deleteRow();
				}
				return true;
			});

			// collect the processes that are used
			var usedProcesses = new TLongObjectHashMap<TLongHashSet>();
			NativeSql.QueryResultHandler usedProcHandler = r -> {
				var systemId = r.getLong(1);
				var processId = r.getLong(2);
				var ids = usedProcesses.get(systemId);
				if (ids == null) {
					ids = new TLongHashSet();
					usedProcesses.put(systemId, ids);
				}
				ids.add(processId);
				return true;
			};
			sql = "select id, f_reference_process from tbl_product_systems";
			nativeSql.query(sql, usedProcHandler);
			sql = "select f_product_system, f_provider from tbl_process_links";
			nativeSql.query(sql,usedProcHandler);

			// remove unused processes from product systems
			sql = "select f_product_system, f_process from tbl_product_system_processes";
			nativeSql.updateRows(sql, r -> {
				var systemId = r.getLong(1);
				var ids = usedProcesses.get(systemId);
				if (ids == null) {
					r.deleteRow();
					return true;
				}
				var processId = r.getLong(2);
				if (!ids.contains(processId)) {
					r.deleteRow();
				}
				return true;
			});

			db.addLibrary(libId);
		} catch (Exception e) {
			throw new RuntimeException("failed to add library", e);
		}
	}

	private List<TechFlow> techFlowsOf(IDatabase db) {
		var processes = new ProcessDao(db).getDescriptors()
			.stream()
			.collect(map());
		var flows = new FlowDao(db).getDescriptors()
			.stream()
			.filter(d -> d.flowType != FlowType.ELEMENTARY_FLOW)
			.collect(map());

		var libIdx = library.readTechIndex();
		var list = new ArrayList<TechFlow>();
		for (var i : libIdx.items()) {
			var process = processes.get(i.provider().id());
			var flow = flows.get(i.flow().id());
			if (process != null && flow != null) {
				list.add(TechFlow.of(process, flow));
			}
		}
		return list;
	}


	private <T extends RootDescriptor> Collector<T, ?, Map<String, T>> map() {
		return Collectors.toMap((T d) -> d.refId, (T d) -> d, (T d1, T d2) -> d1);
	}

}
