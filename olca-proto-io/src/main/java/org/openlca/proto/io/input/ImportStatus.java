package org.openlca.proto.io.input;

import java.util.Objects;

import org.openlca.core.model.RootEntity;

public record ImportStatus<T extends RootEntity>(
	T model, Status status, String error) {

	public enum Status {
		CREATED, UPDATED, SKIPPED, ERROR
	}

	public ImportStatus(T model, Status status, String error) {
		this.model = model;
		this.status = Objects.requireNonNull(status);
		this.error = error;
	}

	public boolean isCreated() {
		return status == Status.CREATED;
	}

	public boolean isUpdated() {
		return status == Status.UPDATED;
	}

	public boolean isSkipped() {
		return status == Status.SKIPPED;
	}

	public boolean isError() {
		return status == Status.ERROR;
	}
}
