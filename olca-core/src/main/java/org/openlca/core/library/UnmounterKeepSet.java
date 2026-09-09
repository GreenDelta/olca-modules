package org.openlca.core.library;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ModelReferences;
import org.openlca.core.library.reader.LibReader;
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
			return keepSet;
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
