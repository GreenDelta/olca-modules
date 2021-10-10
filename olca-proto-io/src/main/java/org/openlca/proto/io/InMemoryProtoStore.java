package org.openlca.proto.io;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.google.protobuf.AbstractMessage;
import org.openlca.core.model.ModelType;
import org.openlca.proto.Proto;
import org.openlca.proto.ProtoActor;
import org.openlca.proto.ProtoCategory;
import org.openlca.proto.ProtoCurrency;
import org.openlca.proto.ProtoDQSystem;
import org.openlca.proto.ProtoFlow;
import org.openlca.proto.ProtoFlowProperty;
import org.openlca.proto.ProtoImpactCategory;
import org.openlca.proto.ProtoImpactMethod;
import org.openlca.proto.ProtoLocation;
import org.openlca.proto.ProtoParameter;
import org.openlca.proto.ProtoProcess;
import org.openlca.proto.ProtoProductSystem;
import org.openlca.proto.ProtoProject;
import org.openlca.proto.ProtoSocialIndicator;
import org.openlca.proto.ProtoSource;
import org.openlca.proto.ProtoUnitGroup;

public class InMemoryProtoStore implements ProtoReader, ProtoWriter {

  private final EnumMap<ModelType, HashMap<String, Object>> store;


  private InMemoryProtoStore() {
    store = new EnumMap<>(ModelType.class);
  }

  public static InMemoryProtoStore create() {
    return new InMemoryProtoStore();
  }

  @Override
  public Set<String> getIds(ModelType modelType) {
    if (modelType == null)
      return Collections.emptySet();
    var map = store.get(modelType);
    if (map == null)
      return Collections.emptySet();
    return new HashSet<>(map.keySet());
  }

  @Override
  public ProtoActor getActor(String id) {
    return get(ModelType.ACTOR, ProtoActor.class, id);
  }

  @Override
  public ProtoCategory getCategory(String id) {
    return get(ModelType.CATEGORY, ProtoCategory.class, id);
  }

  @Override
  public ProtoCurrency getCurrency(String id) {
    return get(ModelType.CURRENCY, ProtoCurrency.class, id);
  }

  @Override
  public ProtoDQSystem getDQSystem(String id) {
    return get(ModelType.DQ_SYSTEM, ProtoDQSystem.class, id);
  }

  @Override
  public ProtoFlow getFlow(String id) {
    return get(ModelType.FLOW, ProtoFlow.class, id);
  }

  @Override
  public ProtoFlowProperty getFlowProperty(String id) {
    return get(ModelType.FLOW_PROPERTY, ProtoFlowProperty.class, id);
  }

  @Override
  public ProtoImpactCategory getImpactCategory(String id) {
    return get(ModelType.IMPACT_CATEGORY, ProtoImpactCategory.class, id);
  }

  @Override
  public ProtoImpactMethod getImpactMethod(String id) {
    return get(ModelType.IMPACT_METHOD, ProtoImpactMethod.class, id);
  }

  @Override
  public ProtoLocation getLocation(String id) {
    return get(ModelType.LOCATION, ProtoLocation.class, id);
  }

  @Override
  public ProtoParameter getParameter(String id) {
    return get(ModelType.PARAMETER, ProtoParameter.class, id);
  }

  @Override
  public ProtoProcess getProcess(String id) {
    return get(ModelType.PROCESS, ProtoProcess.class, id);
  }

  @Override
  public ProtoProductSystem getProductSystem(String id) {
    return get(ModelType.PRODUCT_SYSTEM, ProtoProductSystem.class, id);
  }

  @Override
  public ProtoProject getProject(String id) {
    return get(ModelType.PROJECT, ProtoProject.class, id);
  }

  @Override
  public ProtoSocialIndicator getSocialIndicator(String id) {
    return get(ModelType.SOCIAL_INDICATOR, ProtoSocialIndicator.class, id);
  }

  @Override
  public ProtoSource getSource(String id) {
    return get(ModelType.SOURCE, ProtoSource.class, id);
  }

  @Override
  public ProtoUnitGroup getUnitGroup(String id) {
    return get(ModelType.UNIT_GROUP, ProtoUnitGroup.class, id);
  }

  private <T extends AbstractMessage> T get(
    ModelType modelType, Class<T> protoType, String id) {
    var map = store.get(modelType);
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto != null
      ? protoType.cast(proto)
      : null;
  }

  @Override
  public void putActor(ProtoActor proto) {
    if (proto == null) return;
    put(ModelType.ACTOR, proto, proto::getId);
  }

  @Override
  public void putCategory(ProtoCategory proto) {
    if (proto == null) return;
    put(ModelType.CATEGORY, proto, proto::getId);
  }

  @Override
  public void putCurrency(ProtoCurrency proto) {
    if (proto == null) return;
    put(ModelType.CURRENCY, proto, proto::getId);
  }

  @Override
  public void putDQSystem(ProtoDQSystem proto) {
    if (proto == null) return;
    put(ModelType.DQ_SYSTEM, proto, proto::getId);
  }

  @Override
  public void putFlow(ProtoFlow proto) {
    if (proto == null) return;
    put(ModelType.FLOW, proto, proto::getId);
  }

  @Override
  public void putFlowProperty(ProtoFlowProperty proto) {
    if (proto == null) return;
    put(ModelType.FLOW_PROPERTY, proto, proto::getId);
  }

  @Override
  public void putImpactCategory(ProtoImpactCategory proto) {
    if (proto == null) return;
    put(ModelType.IMPACT_CATEGORY, proto, proto::getId);
  }

  @Override
  public void putImpactMethod(ProtoImpactMethod proto) {
    if (proto == null) return;
    put(ModelType.IMPACT_METHOD, proto, proto::getId);
  }

  @Override
  public void putLocation(ProtoLocation proto) {
    if (proto == null) return;
    put(ModelType.LOCATION, proto, proto::getId);
  }

  @Override
  public void putParameter(ProtoParameter proto) {
    if (proto == null) return;
    put(ModelType.PARAMETER, proto, proto::getId);
  }

  @Override
  public void putProcess(ProtoProcess proto) {
    if (proto == null) return;
    put(ModelType.PROCESS, proto, proto::getId);
  }

  @Override
  public void putProductSystem(ProtoProductSystem proto) {
    if (proto == null) return;
    put(ModelType.PRODUCT_SYSTEM, proto, proto::getId);
  }

  @Override
  public void putProject(ProtoProject proto) {
    if (proto == null) return;
    put(ModelType.PROJECT, proto, proto::getId);
  }

  @Override
  public void putSocialIndicator(ProtoSocialIndicator proto) {
    if (proto == null) return;
    put(ModelType.SOCIAL_INDICATOR, proto, proto::getId);
  }

  @Override
  public void putSource(ProtoSource proto) {
    if (proto == null) return;
    put(ModelType.SOURCE, proto, proto::getId);
  }

  @Override
  public void putUnitGroup(ProtoUnitGroup proto) {
    if (proto == null) return;
    put(ModelType.UNIT_GROUP, proto, proto::getId);
  }

  private void put(ModelType type, AbstractMessage proto, Supplier<String> id) {
    var map = store.computeIfAbsent(type, $ -> new HashMap<>());
    map.put(id.get(), proto);
  }

}
