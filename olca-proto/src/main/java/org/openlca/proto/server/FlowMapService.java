package org.openlca.proto.server;

import java.util.Arrays;
import java.util.function.Consumer;

import com.google.protobuf.ProtocolStringList;
import io.grpc.stub.StreamObserver;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingFileDao;
import org.openlca.core.model.MappingFile;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowMapEntry;
import org.openlca.io.maps.FlowRef;
import org.openlca.proto.Messages;
import org.openlca.proto.Proto;
import org.openlca.proto.input.In;
import org.openlca.proto.output.Out;
import org.openlca.proto.services.FlowMapServiceGrpc;
import org.openlca.proto.services.Services;
import org.openlca.util.Pair;
import org.openlca.util.Strings;

class FlowMapService extends FlowMapServiceGrpc.FlowMapServiceImplBase {

  private final IDatabase db;

  FlowMapService(IDatabase db) {
    this.db = db;
  }

  @Override
  public void getAll(Services.Empty req, StreamObserver<Services.FlowMapInfo> resp) {
    new MappingFileDao(db).getNames()
      .stream()
      .sorted(Strings::compare)
      .map(name -> Services.FlowMapInfo
        .newBuilder()
        .setName(name)
        .build())
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void get(Services.FlowMapInfo req,
                  StreamObserver<Services.FlowMapStatus> resp) {
    Consumer<String> onError = error -> {
      var status = Services.FlowMapStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    var p = getExistingOrError(req.getName());
    var mapping = p.first;
    if (mapping == null) {
      onError.accept("Flow map '"
        + req.getName() + "' does not exist");
      return;
    }

    var flowMap = FlowMap.of(mapping);
    var status = Services.FlowMapStatus.newBuilder()
      .setOk(true)
      .setFlowMap(toProto(flowMap))
      .build();
    resp.onNext(status);
    resp.onCompleted();
  }

  @Override
  public void delete(Services.FlowMapInfo req,
                     StreamObserver<Services.Status> resp) {
    var p = getExistingOrError(req.getName());
    var mapping = p.first;
    if (mapping == null) {
      Response.error(resp, p.second);
      return;
    }
    try {
      new MappingFileDao(db).delete(mapping);
      Response.ok(resp);
    } catch (Exception e) {
      Response.error(resp, "Failed to delete mapping with name='"
        + req.getName() + "' from database");
    }
  }

  private Pair<MappingFile, String> getExistingOrError(String name) {

    if (Strings.nullOrEmpty(name)) {
      var err = "No name of the flow map was given.";
      return Pair.of(null, err);
    }

    // find the existing mapping with this name
    var dao = new MappingFileDao(db);
    var existing = dao.getNames()
      .stream()
      .filter(name::equalsIgnoreCase)
      .findAny()
      .orElse(null);
    if (existing == null) {
      var err = "A flow map '" + name + "' does not exist";
      return Pair.of(null, err);
    }

    // load the mapping
    var mapping = dao.getForName(existing);
    if (mapping == null) {
      var err = "Failed to load flow map '" + name + "'";
      return Pair.of(null, err);
    }
    return Pair.of(mapping, null);
  }

  @Override
  public void put(Proto.FlowMap proto, StreamObserver<Services.Status> resp) {
    var model = toModel(proto);
    if (Strings.nullOrEmpty(model.name)) {
      Response.error(resp, "A name of the flow map is required");
      return;
    }

    var dao = new MappingFileDao(db);

    // check if we should update an existing map
    var p = getExistingOrError(model.name);
    var mapping = p.first;
    if (mapping != null) {
      try {
        model.updateContentOf(mapping);
        dao.update(mapping);
        Response.ok(resp);
      } catch (Exception e) {
        Response.error(resp, "Failed to update existing" +
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
      Response.error(resp, "Failed to save mapping "
        + model.name + ": " + e.getMessage());
    }
  }

  private FlowMap toModel(Proto.FlowMap proto) {
    if (proto == null)
      return FlowMap.empty();
    var flowMap = new FlowMap();
    flowMap.name = proto.getName();
    flowMap.description = proto.getDescription();
    flowMap.refId = proto.getId();
    for (var protoEntry : proto.getMappingsList()) {
      var entry = new FlowMapEntry();
      entry.factor = protoEntry.getConversionFactor();
      entry.sourceFlow = toModelRef(protoEntry.getFrom());
      entry.targetFlow = toModelRef(protoEntry.getTo());
      flowMap.entries.add(entry);
    }
    return flowMap;
  }

  private FlowRef toModelRef(Proto.FlowMapRef protoRef) {
    var flowRef = new FlowRef();
    if (Messages.isEmpty(protoRef))
      return flowRef;

    // flow information
    flowRef.flow = In.descriptorOf(protoRef.getFlow());
    flowRef.flowCategory = categoryPathOf(
      protoRef.getFlow().getCategoryPathList());
    flowRef.flowLocation = Strings.orNull(
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
      flowRef.provider = In.descriptorOf(provider);
      flowRef.providerCategory = categoryPathOf(
        provider.getCategoryPathList());
      flowRef.providerLocation = Strings.orNull(
        provider.getLocation());
    }

    return flowRef;
  }

  private String categoryPathOf(ProtocolStringList categories) {
    if (categories == null || categories.isEmpty())
      return null;
    return categories.stream()
      .reduce(null, (path, elem) ->
        Strings.nullOrEmpty(path)
          ? elem
          : path + "/" + elem);
  }

  private Proto.FlowMap toProto(FlowMap model) {
    var proto = Proto.FlowMap.newBuilder();
    if (model == null)
      return proto.build();
    proto.setId(Strings.orEmpty(model.refId));
    proto.setName(Strings.orEmpty(model.name));
    proto.setDescription(Strings.orEmpty(model.name));

    for (var entry : model.entries) {
      var protoEntry = Proto.FlowMapEntry.newBuilder();
      protoEntry.setConversionFactor(entry.factor);
      if (entry.sourceFlow != null) {
        protoEntry.setFrom(toProtoRef(entry.sourceFlow));
      }
      if (entry.targetFlow != null) {
        protoEntry.setTo(toProtoRef(entry.targetFlow));
      }
      proto.addMappings(protoEntry);
    }

    return proto.build();
  }

  private Proto.FlowMapRef toProtoRef(FlowRef flowRef) {
    var proto = Proto.FlowMapRef.newBuilder();
    if (flowRef == null || flowRef.flow == null)
      return proto.build();

    // flow
    var protoFlow = Out.flowRefOf(flowRef.flow);
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
        proto.setFlowProperty(Out.refOf(flowRef.property));
    }
    if (flowRef.unit != null) {
      proto.setUnit(Out.refOf(flowRef.unit));
    }

    // provider
    if (flowRef.provider != null) {
      var protoProv = Out.processRefOf(flowRef.provider);
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













