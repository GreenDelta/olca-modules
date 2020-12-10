package org.openlca.proto;

import java.io.File;

import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.proto.input.ProtoImport;

public class ImportExample {

  public static void main(String[] args) {
    var zipPath = "C:/Users/ms/Desktop/rems/test_json.zip";
    var dbPath = "C:/Users/ms/openLCA-data-1.4/databases/proto_test";
    try (var store = ZipStore.open(new File(zipPath));
        var db = new DerbyDatabase(new File(dbPath))) {
        var imp = new ProtoImport(store, db);
        imp.run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
