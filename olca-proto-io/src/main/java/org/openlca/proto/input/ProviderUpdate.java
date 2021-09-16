package org.openlca.proto.input;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;

class ProviderUpdate implements Runnable {

  private final IDatabase db;
  private final List<Link> links = new ArrayList<>();

  ProviderUpdate(IDatabase db) {
    this.db = db;
  }

  void add(Link link) {
    if (link == null)
      return;
    links.add(link);
  }

  @Override
  public void run() {
    if (links.isEmpty())
      return;

    // first transform the links into a fast map
    var processIDs = new ProcessDao(db)
      .getDescriptors()
      .stream()
      .collect(Collectors.toMap(
        d -> d.refId,
        d -> d.id
      ));
    var map = new TLongObjectHashMap<TIntLongHashMap>();
    for (var link : links) {
      if (link.exchangeID == 0)
        continue;
      var processID = processIDs.get(link.processID);
      if (processID == null)
        continue;
      var providerID = processIDs.get(link.providerID);
      if (providerID == null)
        continue;
      var providers = map.get(processID);
      if (providers == null) {
        providers = new TIntLongHashMap();
        map.put(processID, providers);
      }
      providers.put(link.exchangeID, providerID);
    }

    // update the exchanges table in a single table scan
    var sql = "select f_owner, internal_id, " +
      "f_default_provider from tbl_exchanges";
    NativeSql.on(db).updateRows(sql, r -> {
      long processID = r.getLong(1);
      int exchangeID = r.getInt(2);
      var providers = map.get(processID);
      if (providers == null)
        return true;
      long provider = providers.get(exchangeID);
      if (provider == 0L)
        return true;
      r.updateLong(3, provider);
      r.updateRow();
      return true;
    });

    this.links.clear();
  }

  static class Link {

    String processID;
    int exchangeID;
    String providerID;

    static Link forProcess(String refID) {
      var link = new Link();
      link.processID = refID;
      return link;
    }

    Link withExchangeID(int id) {
      this.exchangeID = id;
      return this;
    }

    Link withProvider(String refID) {
      this.providerID = refID;
      return this;
    }
  }
}
