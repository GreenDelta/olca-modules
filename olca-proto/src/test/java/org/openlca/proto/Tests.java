package org.openlca.proto;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;

public class Tests {

  private static IDatabase db;

  public static IDatabase db() {
    if (db != null)
      return db;
    db = DerbyDatabase.createInMemory();
    return db;
  }

}
