package org.openlca.proto.io.input;

import org.openlca.core.model.RootEntity;

record ImportItem<T extends RootEntity>(
	ProtoWrap<?> proto, T entity, State state) {

	enum State {
		NEW,
		UPDATE,
		VISITED,
		ERROR
	}

	static <T extends RootEntity> ImportItem<T> newOf(ProtoWrap<?> proto) {
		return new ImportItem<>(proto, null, State.NEW);
	}

	static <T extends RootEntity> ImportItem<T> update(
		ProtoWrap<?> proto, T entity) {
		return new ImportItem<>(proto, entity, State.UPDATE);
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
