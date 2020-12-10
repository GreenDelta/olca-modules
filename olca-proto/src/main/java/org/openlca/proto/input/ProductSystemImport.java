package org.openlca.proto.input;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

public class ProductSystemImport {

  private final ProtoImport imp;

  public ProductSystemImport(ProtoImport imp) {
    this.imp = imp;
  }

  public ProductSystem of(String id) {
    if (id == null)
      return null;
    var sys = imp.get(ProductSystem.class, id);

    // check if we are in update mode
    var update = false;
    if (sys != null) {
      update = imp.shouldUpdate(sys);
      if (!update) {
        return sys;
      }
    }

    // check the proto object
    var proto = imp.store.getProductSystem(id);
    if (proto == null)
      return sys;
    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(sys, wrap))
        return sys;
    }

    // map the data
    if (sys == null) {
      sys = new ProductSystem();
    }
    wrap.mapTo(sys, imp);
    map(proto, sys);

    // insert it
    var dao = new ProductSystemDao(imp.db);
    sys = update
      ? dao.update(sys)
      : dao.insert(sys);
    imp.putHandled(sys);
    return sys;
  }

  private void map(Proto.ProductSystem proto, ProductSystem sys) {

    // sync processes
    var processes = syncProcesses(proto);
    sys.processes.clear();
    processes.values().forEach(d -> sys.processes.add(d.id));

    // collecting the flows
    var flows = new FlowDao(imp.db)
      .getDescriptors()
      .stream()
      .filter(f -> f.flowType != FlowType.ELEMENTARY_FLOW)
      .collect(Collectors.toMap(
        d -> d.refId,
        d -> d
      ));

    // we index the process links first in a structure
    // process ID -> internal exchange ID -> link
    // after we have created and indexed the links, we
    // scan the exchange table and assign the database
    // internal IDs to the links and add them to the
    // product system.
    var index = new TLongObjectHashMap<TIntObjectHashMap<ProcessLink>>();

    for (var protoLink : proto.getProcessLinksList()) {
      var link = new ProcessLink();

      // provider
      var provider = processes.get(protoLink.getProvider().getId());
      if (provider == null)
        continue;
      link.providerId = provider.id;
      link.isSystemLink = provider.type == ModelType.PRODUCT_SYSTEM;

      // flow
      var flow = flows.get(protoLink.getFlow().getId());
      if (flow == null)
        continue;
      link.flowId = flow.id;

      // process
      var process = processes.get(protoLink.getProcess().getId());
      if (process == null)
        continue;
      link.processId = process.id;

      // exchange
      var internalID = protoLink.getExchange().getInternalId();
      if (internalID == 0)
        continue;

      // index the link
      var idx = index.get(process.id);
      if (idx == null) {
        idx = new TIntObjectHashMap<>();
        index.put(process.id, idx);
      }
      idx.put(internalID, link);
    }

    // add the indexed links
    var sql = "select id, f_owner, internal_id from tbl_exchanges";
    NativeSql.on(imp.db).query(sql, r -> {
      var processID = r.getLong(2);
      var idx = index.get(processID);
      if (idx == null)
        return true;

      var internalID = r.getInt(3);
      var link = idx.remove(internalID);
      if (link == null)
        return true;

      link.exchangeId = r.getLong(1);
      sys.processLinks.add(link);
      return true;
    });

  }

  private Map<String, Descriptor> syncProcesses(Proto.ProductSystem proto) {
    var map = new HashMap<String, Descriptor>();

    // handles a process (or product system) reference
    BiConsumer<String, Boolean> handleRef = (refID, checkForSystem) -> {
      if (Strings.nullOrEmpty(refID))
        return;
      if (map.containsKey(refID))
        return;

      var process = new ProcessImport(imp).of(refID);
      if (process != null) {
        map.put(refID, Descriptor.of(process));
        return;
      }

      // providers of links can also be product systems
      if (checkForSystem) {
        var sys = new ProductSystemImport(imp).of(refID);
        if (sys != null) {
          map.put(refID, Descriptor.of(sys));
        }
      }
    };

    handleRef.accept(proto.getReferenceProcess().getId(), false);
    for (var ref : proto.getProcessesList()) {
      handleRef.accept(ref.getId(), true);
    }
    for (var link : proto.getProcessLinksList()) {
      handleRef.accept(link.getProvider().getId(), true);
      handleRef.accept(link.getProcess().getId(), false);
    }
    return map;
  }
}
