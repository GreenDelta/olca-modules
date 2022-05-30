package org.openlca.core.library;

import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.persistence.Table;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

/**
 * Checks if a library and the dependencies of that library can be mounted to
 * the given database.
 */
public record MountCheck(IDatabase db, Library library)
	implements Callable<MountCheck.State> {

	public interface State {
		default boolean isOk() {
			return false;
		}

		default boolean isUsed() {
			return false;
		}

		default boolean isError() {
			return false;
		}

		default String error() {
			return null;
		}
	}

	private enum StandardState implements State {
		OK {
			@Override
			public boolean isOk() {
				return true;
			}
		},

		USED {
			@Override
			public boolean isUsed() {
				return true;
			}
		}
	}

	private record Error(String error) implements State {
		@Override
		public boolean isError() {
			return true;
		}

		@Override
		public String error() {
			return error;
		}
	}

	public static State check(IDatabase db, Library lib) {
		return db == null || lib == null
			? new Error("database or library is null")
			: new MountCheck(db, lib).call();
	}

	@Override
	public State call() {
		for (var lib : Libraries.dependencyOrderOf(library)) {
			var state = execOn(lib);
			if (!state.isOk())
				return state;
		}
		return StandardState.OK;
	}

	private State execOn(Library lib) {
		try (var zip = lib.openJsonZip()) {
			for (var type : ModelType.values()) {
				if (!type.isRoot())
					continue;
				var libIds = new HashSet<>(zip.getRefIds(type));
				if (libIds.isEmpty())
					continue;
				var table = type.getModelClass().getAnnotation(Table.class);
				if (table == null)
					continue;
				var query = "select ref_id from " + table.name();
				var used = new AtomicBoolean(false);
				NativeSql.on(db).query(query, r -> {
					var refId = r.getString(1);
					if (refId != null && libIds.contains(refId)) {
						used.set(true);
						return false;
					}
					return true;
				});
				if (used.get()) {
					return StandardState.USED;
				}
			}
			return StandardState.OK;

		} catch (Exception e) {
			return new Error(e.getMessage());
		}
	}

}
