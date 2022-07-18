package org.openlca.jsonld.input;

import com.google.gson.JsonObject;
import org.openlca.core.model.RootEntity;

record ImportItem<T extends RootEntity>(
	JsonObject json, T entity, State state) {

	enum State {
		NEW,
		UPDATE,
		VISITED,
		ERROR
	}

	static <T extends RootEntity> ImportItem<T> newOf(JsonObject json) {
		return new ImportItem<>(json, null, State.NEW);
	}

	static <T extends RootEntity> ImportItem<T> update(
		JsonObject json, T entity) {
		return new ImportItem<>(json, entity, State.UPDATE);
	}

	static <T extends RootEntity> ImportItem<T> visited(T entity) {
		return new ImportItem<>(null, entity, State.VISITED);
	}

	static <T extends RootEntity> ImportItem<T> error() {
		return new ImportItem<>(null, null, State.ERROR);
	}

	boolean isError() {
		return state == State.ERROR;
	}

	boolean isVisited() {
		return state == State.VISITED;
	}

	boolean isNew() {
		return state == State.NEW;
	}

}
