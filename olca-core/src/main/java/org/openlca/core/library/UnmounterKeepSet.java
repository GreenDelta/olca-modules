package org.openlca.core.library;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ModelReferences;
import org.openlca.core.library.reader.LibReader;
import org.openlca.core.matrix.index.EnviFlow;
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

			// library processes and impact categories are stored as stubs in the
			// database (without exchanges and CFs) so their dependencies (default
			// providers, flows, locations) only exist in the library matrices; add
			// them to the keep set here
			scanLibraryData(refs, keepSet);
			return keepSet;
		}

		/// Adds the data sets to the keep set that are only visible in the library
		/// matrices: the default providers (matrix A) and the elementary flows and
		/// locations (matrix B) of used processes as well as the elementary flows
		/// and locations (matrix C) of used impact categories.
		private void scanLibraryData(
			ModelReferences refs, Map<ModelType, Set<String>> keepSet
		) {
			var techIdx = libReader.techIndex();
			var enviIdx = libReader.enviIndex();

			// the column of a library process in A and B is the position of its
			// provider flow in the tech index
			var colOf = new HashMap<String, Integer>();
			if (techIdx != null) {
				for (int i = 0; i < techIdx.size(); i++) {
					var tf = techIdx.at(i);
					var p = tf.provider();
					if (p.type == ModelType.PROCESS && lib.equals(p.library)) {
						colOf.put(p.refId, i);
					}
				}
			}

			// default providers, flows and locations of used processes
			var procKeep = keepSet
				.computeIfAbsent(ModelType.PROCESS, _ -> new HashSet<>());
			var queue = new ArrayDeque<Integer>();
			var scanned = new HashSet<Integer>();
			for (var e : colOf.entrySet()) {
				if (procKeep.contains(e.getKey())) {
					queue.add(e.getValue());
				}
			}

			while (!queue.isEmpty()) {
				int j = queue.poll();
				if (!scanned.add(j))
					continue;

				// matrix A: default providers of a used process are used too
				var colA = libReader.columnOf(LibMatrix.A, j);
				if (colA != null) {
					for (int i = 0; i < colA.length; i++) {
						// the diagonal is the own provider flow of the process
						if (i == j || colA[i] == 0)
							continue;
						var tf = techIdx.at(i);
						var p = tf.provider();
						if (p.type != ModelType.PROCESS || !lib.equals(p.library))
							continue;
						if (procKeep.add(p.refId)) {
							keep(refs, keepSet, ModelType.PROCESS, p.refId);
							var col = colOf.get(p.refId);
							if (col != null)
								queue.add(col);
						}
					}
				}

				// matrix B: elementary flows and locations of a used process
				var colB = libReader.columnOf(LibMatrix.B, j);
				if (colB != null && enviIdx != null) {
					for (int k = 0; k < colB.length; k++) {
						if (colB[k] != 0)
							keepEnviFlow(refs, keepSet, enviIdx.at(k));
					}
				}
			}

			// matrix C: elementary flows and locations of used impact categories
			var impacts = libReader.impactIndex();
			var catKeep = keepSet.get(ModelType.IMPACT_CATEGORY);
			if (impacts != null && catKeep != null && enviIdx != null) {
				var mC = libReader.matrixOf(LibMatrix.C);
				if (mC != null) {
					for (int h = 0; h < impacts.size(); h++) {
						var cat = impacts.at(h);
						if (cat == null || !catKeep.contains(cat.refId))
							continue;
						var row = mC.getRow(h);
						for (int k = 0; k < row.length; k++) {
							if (row[k] != 0)
								keepEnviFlow(refs, keepSet, enviIdx.at(k));
						}
					}
				}
			}
		}

		private void keepEnviFlow(
			ModelReferences refs,
			Map<ModelType, Set<String>> keepSet,
			@Nullable EnviFlow iFlow
		) {
			if (iFlow == null)
				return;
			var flow = iFlow.flow();
			if (flow != null && lib.equals(flow.library))
				keep(refs, keepSet, ModelType.FLOW, flow.refId);
			var location = iFlow.location();
			if (location != null && lib.equals(location.library))
				keep(refs, keepSet, ModelType.LOCATION, location.refId);
		}

		private void keep(
			ModelReferences refs,
			Map<ModelType, Set<String>> keepSet,
			ModelType type,
			String refId
		) {
			var set = keepSet.computeIfAbsent(type, _ -> new HashSet<>());
			if (set.add(refId))
				keepTreeOf(new TypedRefId(type, refId), refs, keepSet);
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
