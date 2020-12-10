package org.openlca.proto.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.RootEntity;

public class WriterConfig {

  public final IDatabase db;
  public final DependencyHandler dependencies;

  public WriterConfig(IDatabase db, DependencyHandler dependencies) {
    this.db = db;
    this.dependencies = dependencies;
  }

  public static WriterConfig of(IDatabase db) {
    return new WriterConfig(db, new DefaultHandler());
  }

  private static class DefaultHandler implements DependencyHandler {

    @Override
    public void push(RootEntity dependency) {
    }
  }
}
