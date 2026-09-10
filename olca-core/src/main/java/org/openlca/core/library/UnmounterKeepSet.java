package org.openlca.core.library;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ModelReferences;
import org.openlca.core.library.reader.LibReader;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.TypedRefId;
import org.openlca.core.model.descriptors.RootDescriptor;

@NullMarked
class UnmounterKeepSet {

	private final Unmounter.Retention retention;

	@Nullable
	private final EnumMap<ModelType, Set<String>> keep;

	private UnmounterKeepSet(
		Unmounter.Retention retention,
		@Nullable EnumMap<ModelType, Set<String>> keep
	) {
		this.retention = retention;
		this.keep = keep;
	}

	static UnmounterKeepSet of(
		Unmounter.Retention retention,
		IDatabase db,
		@Nullable LibReader reader
	) {
		return switch (retention) {
			case KEEP_ALL, KEEP_NONE -> new UnmounterKeepSet(retention, null);
			case KEEP_USED -> {
				if (reader == null)
					yield new UnmounterKeepSet(retention, null);
				var keepSet = new UsageScan(db, reader).scan();
				yield new UnmounterKeepSet(retention, keepSet);
			}
		};
	}

	boolean has(RootDescriptor d) {
		return switch (retention) {
			case KEEP_ALL -> true;
			case KEEP_NONE -> false;
			case KEEP_USED -> {
				if (keep == null || d.type == null || d.refId == null)
					yield false;
				var set = keep.get(d.type);
				yield set != null && set.contains(d.refId);
			}
		};
	}

	private static class UsageScan {

		private final IDatabase db;
		private final LibReader libReader;
		private final String lib;

		UsageScan(IDatabase db, LibReader libReader) {
			this.db = db;
			this.libReader = libReader;
			this.lib = libReader.libraryName();
		}

		EnumMap<ModelType, Set<String>> scan() {
			var refs = ModelReferences.scan(db);
			var keepSet = new EnumMap<ModelType, Set<String>>(ModelType.class);

			for (var type : ModelType.values()) {
				for (var d : Daos.root(db, type).getDescriptors()) {
					var ref = new TypedRefId(d.type, d.refId);
					if (!lib.equals(d.library) || contains(ref, keepSet))
						continue;
					refs.iterateUsages(ref, usage -> {
						if (!lib.equals(usage.library)) {
							keepTreeOf(ref, refs, keepSet);
							return false;
						}
						return true;
					});
				}
			}

			// Library processes and impact categories are stored as stubs in the
			// database (without exchanges and CFs) so their dependencies (default
			// providers, flows, locations) only exist in the library matrices; add
			// them to the keep set here.
			scanInventoryMatrices(refs, keepSet);
			scanImpactMatrix(refs, keepSet);
			return keepSet;
		}


		private void scanImpactMatrix(
			ModelReferences refs, Map<ModelType, Set<String>> keepSet
		) {

			var used = keepSet.get(ModelType.IMPACT_CATEGORY);
			if (used == null)
				return;

			var impactIdx = libReader.impactIndex();
			var enviIdx = libReader.enviIndex();
			if (impactIdx == null || enviIdx == null)
				return;

			var matrixC = libReader.matrixOf(LibMatrix.C);
			if (matrixC == null)
				return;

			for (int row = 0; row < impactIdx.size(); row++) {
				var cat = impactIdx.at(row);
				if (cat == null || !used.contains(cat.refId))
					continue;
				for (int col = 0; col < enviIdx.size(); col++) {
					if (matrixC.get(row, col) != 0) {
						keepEnviFlow(refs, keepSet, enviIdx.at(col));
					}
				}
			}
		}

		private void scanInventoryMatrices(
			ModelReferences refs, Map<ModelType, Set<String>> keepSet
		) {
			var techIdx = libReader.techIndex();
			var enviIdx = libReader.enviIndex();

			// start with the used providers, if any;
			// go through the matrices A and B to add
			// used flows, locations, and providers;
			// repeat until all providers are visited
			var techCols = columnIndexOf(techIdx);
			var queue = new ArrayDeque<Integer>();
			var visited = new HashSet<Integer>();
			for (var e : techCols.entrySet()) {
				if (contains(e.getKey(), keepSet)) {
					queue.addAll(e.getValue());
				}
			}

			while (!queue.isEmpty()) {
				int j = queue.poll();
				if (!visited.add(j))
					continue;

				var colA = libReader.columnOf(LibMatrix.A, j);
				if (colA == null)
					continue;

				// collect used product and waste flows and default
				// providers (recursively) from matrix A
				for (int i = 0; i < colA.length; i++) {
					if (i == j || colA[i] == 0)
						continue;

					var tf = techIdx.at(i);
					if (tf.provider() == null)
						continue;

					checkKeep(refs, keepSet, tf.flow());
					var providerId = new TypedRefId(
						tf.provider().type, tf.provider().refId);
					if (contains(providerId, keepSet))
						continue;
					checkKeep(refs, keepSet, tf.provider());
					var nextCols = techCols.get(providerId);
					if (nextCols != null) {
						queue.addAll(nextCols);
					}
				}

				// collect used elementary flows and locations from
				// matrix B
				if (enviIdx == null)
					continue;
				var colB = libReader.columnOf(LibMatrix.B, j);
				if (colB == null)
					continue;
				for (int k = 0; k < colB.length; k++) {
					if (colB[k] != 0)
						keepEnviFlow(refs, keepSet, enviIdx.at(k));
				}
			}
		}

		private Map<TypedRefId, List<Integer>> columnIndexOf(
			@Nullable TechIndex techIdx
		) {
			if (techIdx == null)
				return Map.of();
			var map = new HashMap<TypedRefId, List<Integer>>(techIdx.size());
			techIdx.each((col, techFlow) -> {
				var p = techFlow.provider();
				if (p == null)
					return;
				var tid = new TypedRefId(p.type, p.refId);
				map.computeIfAbsent(tid, _ -> new ArrayList<>()).add(col);
			});
			return map;
		}

		private void keepEnviFlow(
			ModelReferences refs,
			Map<ModelType, Set<String>> keepSet,
			@Nullable EnviFlow enviFlow
		) {
			if (enviFlow == null)
				return;
			checkKeep(refs, keepSet, enviFlow.flow());
			checkKeep(refs, keepSet, enviFlow.location());
		}

		private void checkKeep(
			ModelReferences refs,
			Map<ModelType, Set<String>> keepSet,
			@Nullable RootDescriptor d
		) {
			if (d == null
				|| d.type == null
				|| d.refId == null
				|| !lib.equals(d.library))
				return;
			var set = keepSet.computeIfAbsent(d.type, _ -> new HashSet<>());
			if (set.add(d.refId)) {
				// if it was newly added, also add the library references
				keepTreeOf(new TypedRefId(d.type, d.refId), refs, keepSet);
			}
		}

		private void keepTreeOf(
			TypedRefId ref, ModelReferences refs, Map<ModelType, Set<String>> keepSet
		) {
			keepSet.computeIfAbsent(ref.type, _ -> new HashSet<>()).add(ref.refId);
			refs.iterateReferences(ref, next -> {
				if (lib.equals(next.library) && !contains(next, keepSet)) {
					keepTreeOf(next, refs, keepSet);
				}
			});
		}

		private boolean contains(
			TypedRefId ref, Map<ModelType, Set<String>> keepSet
		) {
			var set = keepSet.get(ref.type);
			return set != null && set.contains(ref.refId);
		}
	}
}
