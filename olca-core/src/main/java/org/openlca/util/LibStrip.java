package org.openlca.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.library.reader.LibReader;
import org.openlca.core.matrix.cache.ExchangeTable;
import org.openlca.core.matrix.cache.ProviderMap;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.io.index.IxContext;
import org.openlca.core.matrix.io.index.IxEnviIndex;
import org.openlca.core.matrix.io.index.IxImpactIndex;
import org.openlca.core.matrix.io.index.IxTechIndex;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.jsonld.ZipStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibStrip implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final IDatabase db;
	private final LibReader lib;

	private LibStrip(IDatabase db, LibReader lib) {
		this.db = Objects.requireNonNull(db);
		this.lib = Objects.requireNonNull(lib);
	}

	public static LibStrip of(IDatabase db, LibReader lib) {
		return new LibStrip(db, lib);
	}

	@Override
	public void run() {
		var all = lib.techIndex();
		if (all == null || all.isEmpty()) {
			log.error("library has not processes in database; aborted");
			return;
		}

		var used = getUsedLibraryProviders();
		if (used.isEmpty()) {
			log.error("library processes are not used; aborted");
			return;
		}

		var strippedIdx = new TechIndex();
		int[] idxMap = new int[all.size()];
		for (int i = 0; i < idxMap.length; i++) {
			var techFlow = all.at(i);
			idxMap[i] = used.contains(techFlow)
					? strippedIdx.add(techFlow)
					: -1;
		}

		if (strippedIdx.isEmpty() || strippedIdx.size() == all.size()) {
			log.error("could not reduce the library index");
			return;
		}

		var stripped = LibraryDir.of(lib.library().folder().getParentFile())
				.create(lib.libraryName() + "_stripped");
		var ixCxt = IxContext.of(db);
		int n = strippedIdx.size();

		// tech. index and matrix
		IxTechIndex.of(strippedIdx, ixCxt)
				.writeToDir(stripped.folder());
		var matrixA = new HashPointMatrix(n, n);
		for (int i = 0; i < n; i++) {
			matrixA.set(i, i, 1.0);
		}
		LibMatrix.A.write(stripped, matrixA);
		LibMatrix.INV.write(stripped, matrixA);

		// envi. index and matrix
		var flowIdx = lib.enviIndex();
		if (!flowIdx.isEmpty()) {
			IxEnviIndex.of(flowIdx, ixCxt)
					.writeToDir(stripped.folder());

			var rawM = lib.matrixOf(LibMatrix.M);
			if (rawM != null) {
				var strippedB = new DenseMatrix(flowIdx.size(), n);
				for (int col = 0; col < idxMap.length; col++) {
					int target = idxMap[col];
					if (target < 0)
						continue;
					var vals = rawM.getColumn(col);
					strippedB.setColumn(target, vals);
				}
				LibMatrix.B.write(stripped, strippedB);
				LibMatrix.M.write(stripped, strippedB);
			}
		}

		// impact index and matrix
		var impactIdx = lib.impactIndex();
		if (impactIdx != null && !impactIdx.isEmpty()) {
			IxImpactIndex.of(impactIdx)
					.writeToDir(stripped.folder());
			var matrixC = lib.matrixOf(LibMatrix.C);
			if (matrixC != null) {
				LibMatrix.C.write(stripped, matrixC);
			}
		}

		// reduce the meta-data
		var strippedProcessIds = new HashSet<String>();
		var strippedFlowIds = new HashSet<String>();
		for (var techFlow : strippedIdx) {
			strippedProcessIds.add(techFlow.provider().refId);
			strippedFlowIds.add(techFlow.flow().refId);
		}
		for (var enviFlow : flowIdx) {
			strippedFlowIds.add(enviFlow.flow().refId);
		}

		try (var sourceZip = lib.library().openJsonZip();
				 var targetZip = ZipStore.open(stripped.getJsonZip())) {
			for (var type : ModelType.values()) {
				var ids = sourceZip.getRefIds(type);
				for (var id : ids) {
					if (type == ModelType.PROCESS && !strippedProcessIds.contains(id))
						continue;
					if (type == ModelType.FLOW && !strippedFlowIds.contains(id))
						continue;
					var json = sourceZip.get(type, id);
					targetZip.put(type, json);
				}
			}
		} catch (Exception e) {
			log.error("failed to strip meta-data package");
		}

		log.info("reduced the library from {} to {} processes",
				all.size(), strippedIdx.size());
	}

	private Set<TechFlow> getUsedLibraryProviders() {
		var processes = ProviderMap.create(db);
		var used = new HashSet<TechFlow>();

		new ExchangeTable(db).each(e -> {
			if (!e.isLinkable() || e.defaultProviderId == 0)
				return;
			var techFlow = processes.getTechFlow(e.defaultProviderId, e.flowId);
			if (techFlow != null && isLib(techFlow.library())) {
				used.add(techFlow);
			}
		});

		for (var system : db.getAll(ProductSystem.class)) {

			var ref = system.referenceProcess;
			if (ref != null && isLib(ref.library)) {
				used.add(TechFlow.of(ref));
				continue;
			}

			for (var link : system.processLinks) {
				var p = processes.getTechFlow(link.providerId, link.flowId);
				if (p == null)
					continue;
				if (isLib(p.library())) {
					used.add(p);
				}
			}
		}

		return used;
	}

	private boolean isLib(String libId) {
		return libId != null && libId.equals(lib.libraryName());
	}

}
