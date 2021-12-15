package org.openlca.proto.io.server;

import java.util.Arrays;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingFileDao;
import org.openlca.core.model.MappingFile;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowMapEntry;
import org.openlca.io.maps.FlowRef;
import org.openlca.proto.ProtoFlowMap;
import org.openlca.proto.ProtoFlowMapEntry;
import org.openlca.proto.ProtoFlowMapRef;
import org.openlca.proto.grpc.FlowMapServiceGrpc;
import org.openlca.proto.grpc.ProtoFlowMapName;
import org.openlca.proto.io.Messages;
import org.openlca.proto.io.input.In;
import org.openlca.proto.io.output.Refs;
import org.openlca.util.Strings;

import com.google.protobuf.Empty;
import com.google.protobuf.ProtocolStringList;

import io.grpc.stub.StreamObserver;

class FlowMapService extends FlowMapServiceGrpc.FlowMapServiceImplBase {

  private final IDatabase db;

  FlowMapService(IDatabase db) {
    this.db = db;
  }

  @Override
  public void getAll(Empty req, StreamObserver<ProtoFlowMapName> resp) {
    new MappingFileDao(db).getNames()
        .stream()
        .sorted(Strings::compare)
        .map(name -> ProtoFlowMapName.newBuilder().setName(name).build())
        .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void get(ProtoFlowMapName req, StreamObserver<ProtoFlowMap> resp) {
    var mapping = forceGet(req.getName(), resp);
    if (mapping == null)
      return;
    var flowMap = FlowMap.of(mapping);
    resp.onNext(toProto(flowMap));
    resp.onCompleted();
  }

  @Override
  public void delete(ProtoFlowMapName req, StreamObserver<Empty> resp) {
    var mapping = forceGet(req.getName(), resp);
    if (mapping == null)
      return;
    try {
      new MappingFileDao(db).delete(mapping);
      Response.ok(resp);
    } catch (Exception e) {
      Response.serverError(resp,
          "Failed to delete mapping with name='"
              + req.getName() + "' from database");
    }
  }

  private MappingFile forceGet(String name, StreamObserver<?> resp) {
    if (Strings.nullOrEmpty(name)) {
      Response.invalidArg(resp,
          "No name of the flow map was given.");
      return null;
    }
    var mapping = findByName(name);
    if (mapping == null) {
      Response.notFound(resp,
          "Could not load flow map '" + name + "'");
    }
    return mapping;
  }

  private MappingFile findByName(String name) {
    var dao = new MappingFileDao(db);
    var existing = dao.getNames()
        .stream()
        .filter(name::equalsIgnoreCase)
        .findAny()
        .orElse(null);
    if (existing == null)
      return null;
    return dao.getForName(existing);
  }

  @Override
  public void put(ProtoFlowMap proto, StreamObserver<Empty> resp) {
    var model = toModel(proto);
    if (Strings.nullOrEmpty(model.name)) {
      Response.invalidArg(resp, "A name of the flow map is required");
      return;
    }

    var dao = new MappingFileDao(db);

    // check if we should update an existing map
    var mapping = findByName(model.name);
    if (mapping != null) {
      try {
        model.updateContentOf(mapping);
        dao.update(mapping);
        Response.ok(resp);
      } catch (Exception e) {
        Response.serverError(resp,
            "Failed to update existing" +
                " flow map " + model.name + ": " + e.getMessage());
      }
      return;
    }

    // save it as new flow map
    mapping = model.toMappingFile();
    try {
      dao.insert(mapping);
      Response.ok(resp);
    } catch (Exception e) {
      Response.serverError(resp,
          "Failed to save mapping "
              + model.name + ": " + e.getMessage());
    }
  }

  private FlowMap toModel(ProtoFlowMap proto) {
    if (proto == null)
      return FlowMap.empty();
    var flowMap = new FlowMap();
    flowMap.name = proto.getName();
    flowMap.description = proto.getDescription();
    flowMap.refId = proto.getId();
    for (var protoEntry : proto.getMappingsList()) {
      var source = toModelRef(protoEntry.getFrom());
      var target = toModelRef(protoEntry.getTo());
      var factor = protoEntry.getConversionFactor();
      flowMap.entries.add(new FlowMapEntry(source, target, factor));
    }
    return flowMap;
  }

  private FlowRef toModelRef(ProtoFlowMapRef protoRef) {
    var flowRef = new FlowRef();
    if (Messages.isEmpty(protoRef))
      return flowRef;

    // flow information
    flowRef.flow = In.fill(new FlowDescriptor(), protoRef.getFlow());
    flowRef.flowCategory = categoryPathOf(
        protoRef.getFlow().getCategoryPathList());
    flowRef.flowLocation = Strings.nullIfEmpty(
        protoRef.getFlow().getLocation());

    // flow property
    var property = protoRef.getFlowProperty();
    if (Messages.isNotEmpty(property)) {
      flowRef.property = In.descriptorOf(property);
    }

    // unit
    var unit = protoRef.getUnit();
    if (Messages.isNotEmpty(unit)) {
      flowRef.unit = In.descriptorOf(unit);
    }

    // provider
    var provider = protoRef.getProvider();
    if (Messages.isNotEmpty(provider)) {
      flowRef.provider = In.fill(
          new ProcessDescriptor(), provider);
      flowRef.providerCategory = categoryPathOf(
          provider.getCategoryPathList());
      flowRef.providerLocation = Strings.nullIfEmpty(
          provider.getLocation());
    }

    return flowRef;
  }

  private String categoryPathOf(ProtocolStringList categories) {
    if (categories == null || categories.isEmpty())
      return null;
    return categories.stream()
        .reduce(null, (path, elem) -> Strings.nullOrEmpty(path)
            ? elem
            : path + "/" + elem);
  }

  private ProtoFlowMap toProto(FlowMap model) {
    var proto = ProtoFlowMap.newBuilder();
    if (model == null)
      return proto.build();
    proto.setId(Strings.orEmpty(model.refId));
    proto.setName(Strings.orEmpty(model.name));
    proto.setDescription(Strings.orEmpty(model.name));

    for (var entry : model.entries) {
      var protoEntry = ProtoFlowMapEntry.newBuilder();
      protoEntry.setConversionFactor(entry.factor());
      if (entry.sourceFlow() != null) {
        protoEntry.setFrom(toProtoRef(entry.sourceFlow()));
      }
      if (entry.targetFlow() != null) {
        protoEntry.setTo(toProtoRef(entry.targetFlow()));
      }
      proto.addMappings(protoEntry);
    }

    return proto.build();
  }

  private ProtoFlowMapRef toProtoRef(FlowRef flowRef) {
    var proto = ProtoFlowMapRef.newBuilder();
    if (flowRef == null || flowRef.flow == null)
      return proto.build();

    // flow
    var protoFlow = Refs.refOf(flowRef.flow);
    if (flowRef.flowCategory != null) {
      Arrays.stream(flowRef.flowCategory.split("/"))
          .filter(Strings::notEmpty)
          .forEach(protoFlow::addCategoryPath);
    }
    if (flowRef.flowLocation != null) {
      protoFlow.setLocation(flowRef.flowLocation);
    }
    proto.setFlow(protoFlow);

    // flow property & unit
    if (flowRef.property != null) {
      proto.setFlowProperty(Refs.refOf(flowRef.property));
    }
    if (flowRef.unit != null) {
      proto.setUnit(Refs.refOf(flowRef.unit));
    }

    // provider
    if (flowRef.provider != null) {
      var protoProv = Refs.refOf(flowRef.provider);
      if (flowRef.providerLocation != null) {
        protoProv.setLocation(flowRef.providerLocation);
      }
      if (flowRef.providerCategory != null) {
        Arrays.stream(flowRef.providerCategory.split("/"))
            .filter(Strings::notEmpty)
            .forEach(protoProv::addCategoryPath);
      }
      proto.setProvider(protoProv);
    }

    return proto.build();
  }
}
