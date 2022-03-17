package org.openlca.proto.io.output;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.proto.ProtoExchangeRef;
import org.openlca.proto.ProtoParameterRedef;
import org.openlca.proto.ProtoParameterRedefSet;
import org.openlca.proto.ProtoProcessLink;
import org.openlca.proto.ProtoProductSystem;
import org.openlca.proto.ProtoType;
import org.openlca.util.Strings;

public class ProductSystemWriter {

  private final WriterConfig config;

  public ProductSystemWriter(WriterConfig config) {
    this.config = config;
  }

  public ProtoProductSystem write(ProductSystem system) {
    var proto = ProtoProductSystem.newBuilder();
    if (system == null)
      return proto.build();
    proto.setType(ProtoType.ProductSystem);
    Out.map(system, proto);
    Out.dep(config, system.category);
    mapQRef(system, proto);
    var processes = mapProcesses(system, proto);
    mapLinks(system, proto, processes);
    mapParameterSets(system, proto);
    return proto.build();
  }

  private void mapQRef(ProductSystem system,
    ProtoProductSystem.Builder proto) {
    // ref. process
    if (system.referenceProcess != null) {
      var p = Refs.refOf(system.referenceProcess);
      proto.setReferenceProcess(p);
      Out.dep(config, system.referenceProcess);
    }

    // ref. exchange
    if (system.referenceExchange != null) {
      var e = ProtoExchangeRef.newBuilder()
        .setInternalId(system.referenceExchange.internalId);
      proto.setReferenceExchange(e);
    }

    // ref. quantity
    if (system.targetFlowPropertyFactor != null) {
      var prop = system.targetFlowPropertyFactor.flowProperty;
      proto.setTargetFlowProperty(Refs.refOf(prop));
    }

    // ref. unit
    if (system.targetUnit != null) {
      proto.setTargetUnit(Refs.refOf(system.targetUnit));
    }

    // ref. amount
    proto.setTargetAmount(system.targetAmount);
  }

  private Map<Long, RootDescriptor> mapProcesses(
    ProductSystem system, ProtoProductSystem.Builder proto) {
    var processes = new ProcessDao(config.db).descriptorMap();
    var systems = new ProductSystemDao(config.db).descriptorMap();
    Map<Long, RootDescriptor> map = new HashMap<>();
    for (var id : system.processes) {
      RootDescriptor d = processes.get(id);
      if (d == null) {
        d = systems.get(id);
      }
      if (d == null)
        continue;
      map.put(id, d);
      proto.addProcesses(Refs.refOf(d));
      Out.dep(config, d);
    }
    return map;
  }

  private void mapLinks(ProductSystem system,
    ProtoProductSystem.Builder proto,
    Map<Long, RootDescriptor> processes) {

    // collect the used flows
    var flowIDs = new HashSet<Long>();
    var usedExchanges = new TLongHashSet();
    for (var link : system.processLinks) {
      flowIDs.add(link.flowId);
      usedExchanges.add(link.exchangeId);
    }
    var flows = new FlowDao(config.db)
      .getDescriptors(flowIDs)
      .stream()
      .collect(Collectors.toMap(d -> d.id, d -> d));

    // collect the used exchanges
    var exchangeIDs = new TLongIntHashMap();
    String sql = "select id, internal_id from tbl_exchanges";
    NativeSql.on(config.db).query(sql, r -> {
      long id = r.getLong(1);
      if (usedExchanges.contains(id)) {
        exchangeIDs.put(id, r.getInt(2));
      }
      return true;
    });


    // add the links
    for (var link : system.processLinks) {
      var protoLink = ProtoProcessLink.newBuilder();

      // provider
      var provider = processes.get(link.providerId);
      if (provider != null) {
        protoLink.setProvider(Refs.tinyRefOf(provider));
        Out.dep(config, provider);
      }

      // process
      var process = processes.get(link.processId);
      if (process != null) {
        protoLink.setProcess(Refs.tinyRefOf(process));
        Out.dep(config, process);
      }

      // flow
      var flow = flows.get(link.flowId);
      if (flow != null) {
        protoLink.setFlow(Refs.tinyRefOf(flow));
      }

      // linked exchange
      var eid = exchangeIDs.get(link.exchangeId);
      if (eid != 0) {
        protoLink.setExchange(
          ProtoExchangeRef.newBuilder()
            .setInternalId(eid)
            .build());
      }

      // add the link
      proto.addProcessLinks(protoLink);
    }
  }

  private void mapParameterSets(ProductSystem system,
    ProtoProductSystem.Builder proto) {
    for (var paramSet : system.parameterSets) {
      var protoSet = ProtoParameterRedefSet.newBuilder();
      protoSet.setName(Strings.orEmpty(paramSet.name));
      protoSet.setDescription(Strings.orEmpty(paramSet.description));
      protoSet.setIsBaseline(paramSet.isBaseline);
      for (var redef : paramSet.parameters) {
        var protoRedef = ProtoParameterRedef.newBuilder();
        protoRedef.setName(Strings.orEmpty(redef.name));
        protoRedef.setValue(redef.value);
        if (redef.uncertainty != null) {
          var u = Out.uncertaintyOf(redef.uncertainty);
          protoRedef.setUncertainty(u);
        }
        if (redef.contextId != null) {
          var context = redef.contextType == ModelType.PROCESS
            ? config.db.getDescriptor(Process.class, redef.contextId)
            : config.db.getDescriptor(ImpactCategory.class, redef.contextId);
          if (context != null) {
            protoRedef.setContext(Refs.refOf(context));
          }
        }
        protoSet.addParameters(protoRedef);
      }
      proto.addParameterSets(protoSet);
    }
  }
}
