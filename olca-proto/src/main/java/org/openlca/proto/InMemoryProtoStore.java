package org.openlca.proto;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.google.protobuf.AbstractMessage;
import org.openlca.core.model.ModelType;
import org.openlca.proto.generated.Proto;

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
  public Proto.Actor getActor(String id) {
    return get(ModelType.ACTOR, Proto.Actor.class, id);
  }

  @Override
  public Proto.Category getCategory(String id) {
    return get(ModelType.CATEGORY, Proto.Category.class, id);
  }

  @Override
  public Proto.Currency getCurrency(String id) {
    return get(ModelType.CURRENCY, Proto.Currency.class, id);
  }

  @Override
  public Proto.DQSystem getDQSystem(String id) {
    return get(ModelType.DQ_SYSTEM, Proto.DQSystem.class, id);
  }

  @Override
  public Proto.Flow getFlow(String id) {
    return get(ModelType.FLOW, Proto.Flow.class, id);
  }

  @Override
  public Proto.FlowProperty getFlowProperty(String id) {
    return get(ModelType.FLOW_PROPERTY, Proto.FlowProperty.class, id);
  }

  @Override
  public Proto.ImpactCategory getImpactCategory(String id) {
    return get(ModelType.IMPACT_CATEGORY, Proto.ImpactCategory.class, id);
  }

  @Override
  public Proto.ImpactMethod getImpactMethod(String id) {
    return get(ModelType.IMPACT_METHOD, Proto.ImpactMethod.class, id);
  }

  @Override
  public Proto.Location getLocation(String id) {
    return get(ModelType.LOCATION, Proto.Location.class, id);
  }

  @Override
  public Proto.Parameter getParameter(String id) {
    return get(ModelType.PARAMETER, Proto.Parameter.class, id);
  }

  @Override
  public Proto.Process getProcess(String id) {
    return get(ModelType.PROCESS, Proto.Process.class, id);
  }

  @Override
  public Proto.ProductSystem getProductSystem(String id) {
    return get(ModelType.PRODUCT_SYSTEM, Proto.ProductSystem.class, id);
  }

  @Override
  public Proto.Project getProject(String id) {
    return get(ModelType.PROJECT, Proto.Project.class, id);
  }

  @Override
  public Proto.SocialIndicator getSocialIndicator(String id) {
    return get(ModelType.SOCIAL_INDICATOR, Proto.SocialIndicator.class, id);
  }

  @Override
  public Proto.Source getSource(String id) {
    return get(ModelType.SOURCE, Proto.Source.class, id);
  }

  @Override
  public Proto.UnitGroup getUnitGroup(String id) {
    return get(ModelType.UNIT_GROUP, Proto.UnitGroup.class, id);
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
  public void putActor(Proto.Actor proto) {
    if (proto == null) return;
    put(ModelType.ACTOR, proto, proto::getId);
  }

  @Override
  public void putCategory(Proto.Category proto) {
    if (proto == null) return;
    put(ModelType.CATEGORY, proto, proto::getId);
  }

  @Override
  public void putCurrency(Proto.Currency proto) {
    if (proto == null) return;
    put(ModelType.CURRENCY, proto, proto::getId);
  }

  @Override
  public void putDQSystem(Proto.DQSystem proto) {
    if (proto == null) return;
    put(ModelType.DQ_SYSTEM, proto, proto::getId);
  }

  @Override
  public void putFlow(Proto.Flow proto) {
    if (proto == null) return;
    put(ModelType.FLOW, proto, proto::getId);
  }

  @Override
  public void putFlowProperty(Proto.FlowProperty proto) {
    if (proto == null) return;
    put(ModelType.FLOW_PROPERTY, proto, proto::getId);
  }

  @Override
  public void putImpactCategory(Proto.ImpactCategory proto) {
    if (proto == null) return;
    put(ModelType.IMPACT_CATEGORY, proto, proto::getId);
  }

  @Override
  public void putImpactMethod(Proto.ImpactMethod proto) {
    if (proto == null) return;
    put(ModelType.IMPACT_METHOD, proto, proto::getId);
  }

  @Override
  public void putLocation(Proto.Location proto) {
    if (proto == null) return;
    put(ModelType.LOCATION, proto, proto::getId);
  }

  @Override
  public void putParameter(Proto.Parameter proto) {
    if (proto == null) return;
    put(ModelType.PARAMETER, proto, proto::getId);
  }

  @Override
  public void putProcess(Proto.Process proto) {
    if (proto == null) return;
    put(ModelType.PROCESS, proto, proto::getId);
  }

  @Override
  public void putProductSystem(Proto.ProductSystem proto) {
    if (proto == null) return;
    put(ModelType.PRODUCT_SYSTEM, proto, proto::getId);
  }

  @Override
  public void putProject(Proto.Project proto) {
    if (proto == null) return;
    put(ModelType.PROJECT, proto, proto::getId);
  }

  @Override
  public void putSocialIndicator(Proto.SocialIndicator proto) {
    if (proto == null) return;
    put(ModelType.SOCIAL_INDICATOR, proto, proto::getId);
  }

  @Override
  public void putSource(Proto.Source proto) {
    if (proto == null) return;
    put(ModelType.SOURCE, proto, proto::getId);
  }

  @Override
  public void putUnitGroup(Proto.UnitGroup proto) {
    if (proto == null) return;
    put(ModelType.UNIT_GROUP, proto, proto::getId);
  }

  private void put(ModelType type, AbstractMessage proto, Supplier<String> id) {
    var map = store.computeIfAbsent(type, $ -> new HashMap<>());
    map.put(id.get(), proto);
  }

}
