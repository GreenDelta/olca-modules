package org.openlca.proto.io.input;

import java.util.Objects;

import org.openlca.core.model.CategorizedEntity;

public class ImportStatus<T extends CategorizedEntity> {

  public enum Status {
    CREATED, UPDATED, SKIPPED, ERROR
  }

  private final T model;
  private final Status status;
  private final String error;

  private ImportStatus(T model, Status status, String error) {
    this.model = model;
    this.status = Objects.requireNonNull(status);
    this.error = error;
  }

  public static <T extends CategorizedEntity> ImportStatus<T> created(T model) {
    return new ImportStatus<>(model, Status.CREATED, null);
  }

  public static <T extends CategorizedEntity> ImportStatus<T> updated(T model) {
    return new ImportStatus<>(model, Status.UPDATED, null);
  }

  public static <T extends CategorizedEntity> ImportStatus<T> skipped(T model) {
    return new ImportStatus<>(model, Status.SKIPPED, null);
  }

  public static <T extends CategorizedEntity> ImportStatus<T> error(String error) {
    return new ImportStatus<>(null, Status.ERROR, error);
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

  public String error() {
    return error;
  }

  public T model() {
    return model;
  }

  public Status status() {
    return status;
  }
}
