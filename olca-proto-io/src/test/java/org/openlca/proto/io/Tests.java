package org.openlca.proto.io;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.Derby;

public class Tests {

  private static IDatabase db;

  public static IDatabase db() {
    if (db != null)
      return db;
    synchronized (Tests.class) {
      if (db != null)
        return db;
      db = Derby.createInMemory();
      return db;
    }
  }
}
