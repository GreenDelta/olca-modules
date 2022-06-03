package org.openlca.core.library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import jakarta.persistence.Table;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.ZipStore;
import org.openlca.util.Pair;

/**
 * Checks the state of a library and its dependencies before it is mounted to a
 * database.
 */
public record PreMountCheck(IDatabase db, Library library)
	implements Callable<PreMountCheck.Result> {

	public static Result check(IDatabase db, Library lib) {
		return db == null || lib == null
			? Result.error("database or library is null")
			: new PreMountCheck(db, lib).call();
	}

	@Override
	public Result call() {
		var libs = new ArrayList<Library>();
		var states = new ArrayList<PreMountState>();
		for (var lib : Libraries.dependencyOrderOf(library)) {
			var next = execOn(lib);
			if (next.isError())
				return next;
			libs.addAll(next.libraries);
			states.addAll(next.states);
		}
		return new Result(libs, states, null);
	}

	private Result execOn(Library lib) {
		PreMountState state = null;
		try (var zip = lib.openJsonZip()) {
			for (var type : ModelType.values()) {
				if (!type.isRoot() || type == ModelType.CATEGORY)
					continue;
				var next = new TableState(type).get(lib.name(), zip);
				if (next == null)
					continue;
				state = next.join(state);
				if (state == PreMountState.CONFLICT)
					break;
			}
			return state == null
				? Result.of(lib, PreMountState.NEW)
				: Result.of(lib, state);
		} catch (Exception e) {
			return Result.error(
				"pre-mount check failed for library " + lib.name()
					+ ": " + e.getMessage());
		}
	}

	public static class Result {

		private final List<Library> libraries;
		private final List<PreMountState> states;
		private final String error;

		private Result(
			List<Library> libraries,
			List<PreMountState> states,
			String error) {
			this.libraries = libraries;
			this.states = states;
			this.error = error;
		}

		static Result error(String message) {
			return new Result(
				Collections.emptyList(), Collections.emptyList(), message);
		}

		static Result of(Library library, PreMountState state) {
			return new Result(
				Collections.singletonList(library),
				Collections.singletonList(state),
				null);
		}

		public boolean isError() {
			return error != null;
		}

		public String error() {
			return error;
		}

		public boolean isEmpty() {
			return libraries == null
				|| libraries.isEmpty()
				|| states == null
				|| states.size() != libraries.size();
		}

		public Optional<PreMountState> getState(Library library) {
			for (int i = 0; i < libraries.size(); i++) {
				if (Objects.equals(library, libraries.get(i)))
					return Optional.of(states.get(i));
			}
			return Optional.empty();
		}

		public List<Pair<Library, PreMountState>> getStates() {
			return IntStream.range(0, libraries.size())
				.mapToObj(i -> Pair.of(libraries.get(i), states.get(i)))
				.toList();
		}
	}

	private class TableState {

		private final ModelType type;
		private PreMountState state;

		private TableState(ModelType type) {
			this.type = type;
		}

		private PreMountState get(String libName, ZipStore store) {
			var table = type.getModelClass().getAnnotation(Table.class);
			if (table == null)
				return null;
			var libIds = new HashSet<>(store.getRefIds(type));
			if (libIds.isEmpty())
				return null;
			var visited = new HashSet<String>();
			var query = "select ref_id, library from " + table.name();
			NativeSql.on(db).query(query, r -> {
				var refId = r.getString(1);
				if (refId == null || !libIds.contains(refId))
					return true;
				visited.add(refId);
				var lib = r.getString(2);
				state = Objects.equals(lib, libName)
					? PreMountState.PRESENT.join(state)
					: PreMountState.TAG_CONFLICT.join(state);
				return state != PreMountState.CONFLICT;
			});

			return visited.size() < libIds.size()
				? PreMountState.NEW.join(state)
				: state;
		}
	}
}
