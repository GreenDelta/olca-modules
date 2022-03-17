package org.openlca.proto.io.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.descriptors.Descriptor;

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
    public void push(RefEntity dependency) {
    }

    @Override
    public void push(Descriptor descriptor) {
    }
  }
}
