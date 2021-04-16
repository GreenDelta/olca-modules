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

public class MemStore implements ProtoReader, ProtoWriter {

  private final EnumMap<ModelType, HashMap<String, Object>> store;


  private MemStore() {
    store = new EnumMap<>(ModelType.class);
  }

  public static MemStore create() {
    return new MemStore();
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
  public Proto.Category getCategory(String id) {
    var map = store.get(ModelType.CATEGORY);
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.Category
      ? (Proto.Category) proto
      : null;
  }

  public void putCategory(Proto.Category proto) {
    if (proto == null) return;
    put(ModelType.CATEGORY, proto, proto::getId);
  }

  private void put(ModelType type, AbstractMessage proto, Supplier<String> id) {
    var map = store.computeIfAbsent(type, $ -> new HashMap<>());
    map.put(id.get(), proto);
  }

  @Override
  public Proto.Actor getActor(String id) {
    var map = store.get(ModelType.ACTOR);
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.Actor
      ? (Proto.Actor) proto
      : null;
  }

  public void putActor(Proto.Actor proto) {
    if (proto == null)
      return;
    var map = store.computeIfAbsent(
      ModelType.ACTOR, _key -> new HashMap<>());
    map.put(proto.getId(), proto);
  }

  @Override
  public Proto.Currency getCurrency(String id) {
    var map = store.get(ModelType.CURRENCY);
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.Currency
      ? (Proto.Currency) proto
      : null;
  }

  public void putCurrency(Proto.Currency proto) {
    if (proto == null)
      return;
    var map = store.computeIfAbsent(
      ModelType.CURRENCY, _key -> new HashMap<>());
    map.put(proto.getId(), proto);
  }

  @Override
  public Proto.DQSystem getDQSystem(String id) {
    var map = store.get("dq_systems");
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.DQSystem
      ? (Proto.DQSystem) proto
      : null;
  }

  public void putDQSystem(Proto.DQSystem proto) {
    if (proto == null)
      return;
    var map = store.computeIfAbsent(
      "dq_systems", _key -> new HashMap<>());
    map.put(proto.getId(), proto);
  }

  @Override
  public Proto.Flow getFlow(String id) {
    var map = store.get("flows");
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.Flow
      ? (Proto.Flow) proto
      : null;
  }

  public void putFlow(Proto.Flow proto) {
    if (proto == null)
      return;
    var map = store.computeIfAbsent(
      "flows", _key -> new HashMap<>());
    map.put(proto.getId(), proto);
  }

  @Override
  public Proto.FlowProperty getFlowProperty(String id) {
    var map = store.get("flow_properties");
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.FlowProperty
      ? (Proto.FlowProperty) proto
      : null;
  }

  public void putFlowProperty(Proto.FlowProperty proto) {
    if (proto == null)
      return;
    var map = store.computeIfAbsent(
      "flow_properties", _key -> new HashMap<>());
    map.put(proto.getId(), proto);
  }

  @Override
  public Proto.ImpactCategory getImpactCategory(String id) {
    var map = store.get("lcia_categories");
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.ImpactCategory
      ? (Proto.ImpactCategory) proto
      : null;
  }

  public void putImpactCategory(Proto.ImpactCategory proto) {
    if (proto == null)
      return;
    var map = store.computeIfAbsent(
      "lcia_categories", _key -> new HashMap<>());
    map.put(proto.getId(), proto);
  }

  @Override
  public Proto.ImpactMethod getImpactMethod(String id) {
    var map = store.get("lcia_methods");
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.ImpactMethod
      ? (Proto.ImpactMethod) proto
      : null;
  }

  public void putImpactMethod(Proto.ImpactMethod proto) {
    if (proto == null)
      return;
    var map = store.computeIfAbsent(
      "lcia_methods", _key -> new HashMap<>());
    map.put(proto.getId(), proto);
  }

  @Override
  public Proto.Location getLocation(String id) {
    var map = store.get("locations");
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.Location
      ? (Proto.Location) proto
      : null;
  }

  public void putLocation(Proto.Location proto) {
    if (proto == null)
      return;
    var map = store.computeIfAbsent(
      "locations", _key -> new HashMap<>());
    map.put(proto.getId(), proto);
  }


  @Override
  public Proto.Parameter getParameter(String id) {
    var map = store.get("parameters");
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.Parameter
      ? (Proto.Parameter) proto
      : null;
  }

  public void putParameter(Proto.Parameter proto) {
    if (proto == null)
      return;
    var map = store.computeIfAbsent(
      "parameters", _key -> new HashMap<>());
    map.put(proto.getId(), proto);
  }

  @Override
  public Proto.Process getProcess(String id) {
    var map = store.get("processes");
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.Process
      ? (Proto.Process) proto
      : null;
  }

  public void putProcess(Proto.Process proto) {
    if (proto == null)
      return;
    var map = store.computeIfAbsent(
      "processes", _key -> new HashMap<>());
    map.put(proto.getId(), proto);
  }

  @Override
  public Proto.ProductSystem getProductSystem(String id) {
    var map = store.get("product_systems");
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.ProductSystem
      ? (Proto.ProductSystem) proto
      : null;
  }

  public void putProductSystem(Proto.ProductSystem proto) {
    if (proto == null)
      return;
    var map = store.computeIfAbsent(
      "product_systems", _key -> new HashMap<>());
    map.put(proto.getId(), proto);
  }

  @Override
  public Proto.Project getProject(String id) {
    var map = store.get("projects");
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.Project
      ? (Proto.Project) proto
      : null;
  }

  public void putProject(Proto.Project proto) {
    if (proto == null)
      return;
    var map = store.computeIfAbsent(
      "projects", _key -> new HashMap<>());
    map.put(proto.getId(), proto);
  }

  @Override
  public Proto.SocialIndicator getSocialIndicator(String id) {
    var map = store.get("social_indicators");
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.SocialIndicator
      ? (Proto.SocialIndicator) proto
      : null;
  }

  public void putSocialIndicator(Proto.SocialIndicator proto) {
    if (proto == null)
      return;
    var map = store.computeIfAbsent(
      "social_indicators", _key -> new HashMap<>());
    map.put(proto.getId(), proto);
  }

  @Override
  public Proto.Source getSource(String id) {
    var map = store.get("sources");
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.Source
      ? (Proto.Source) proto
      : null;
  }

  public void putSource(Proto.Source proto) {
    if (proto == null)
      return;
    var map = store.computeIfAbsent(
      "sources", _key -> new HashMap<>());
    map.put(proto.getId(), proto);
  }

  @Override
  public Proto.UnitGroup getUnitGroup(String id) {
    var map = store.get("unit_groups");
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.UnitGroup
      ? (Proto.UnitGroup) proto
      : null;
  }

  public void putUnitGroup(Proto.UnitGroup proto) {
    if (proto == null)
      return;
    var map = store.computeIfAbsent(
      "unit_groups", _key -> new HashMap<>());
    map.put(proto.getId(), proto);
  }

  @Override
  public Proto.NwSet getNwSet(String id) {
    var map = store.get("nw_sets");
    if (map == null)
      return null;
    var proto = map.get(id);
    return proto instanceof Proto.NwSet
      ? (Proto.NwSet) proto
      : null;
  }

  public void putNwSet(Proto.NwSet proto) {
    if (proto == null)
      return;
    var map = store.computeIfAbsent(
      "nw_sets", _key -> new HashMap<>());
    map.put(proto.getId(), proto);
  }

  @Override
  public void close() {
    store.clear();
  }
}
