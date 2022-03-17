package org.openlca.proto.io.server;

import java.util.Objects;
import java.util.Optional;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.proto.ProtoRef;
import org.openlca.proto.ProtoType;
import org.openlca.proto.grpc.ProtoDataSet;
import org.openlca.proto.io.InMemoryProtoStore;
import org.openlca.proto.io.ProtoReader;
import org.openlca.proto.io.input.In;
import org.openlca.proto.io.output.ActorWriter;
import org.openlca.proto.io.output.CategoryWriter;
import org.openlca.proto.io.output.CurrencyWriter;
import org.openlca.proto.io.output.DQSystemWriter;
import org.openlca.proto.io.output.FlowPropertyWriter;
import org.openlca.proto.io.output.FlowWriter;
import org.openlca.proto.io.output.ImpactCategoryWriter;
import org.openlca.proto.io.output.ImpactMethodWriter;
import org.openlca.proto.io.output.LocationWriter;
import org.openlca.proto.io.output.ParameterWriter;
import org.openlca.proto.io.output.ProcessWriter;
import org.openlca.proto.io.output.ProductSystemWriter;
import org.openlca.proto.io.output.ProjectWriter;
import org.openlca.proto.io.output.SocialIndicatorWriter;
import org.openlca.proto.io.output.SourceWriter;
import org.openlca.proto.io.output.UnitGroupWriter;
import org.openlca.proto.io.output.WriterConfig;
import org.openlca.util.Strings;

import io.grpc.stub.StreamObserver;

class DataUtil {

  static Category getCategory(
    IDatabase db, ModelType type, String idOrPath) {
    var dao = new CategoryDao(db);
    var cat = dao.getForRefId(idOrPath);
    return cat == null
      ? dao.getForPath(type, idOrPath)
      : cat;
  }

  /**
   * Returns the corresponding model type if the class of the model type is a
   * root entity type so that modelType.getModelClass() != null. If this is not
   * the case, null is returned and a corresponding error is written to the
   * response.
   */
  static ModelType forceRootTypeOf(ProtoType type, StreamObserver<?> resp) {
    var modelType = In.modelTypeOf(type);
    if (modelType == null
        || modelType.getModelClass() == null) {
      Response.invalidArg(resp, "Invalid model type: " + type);
      return null;
    }
    return modelType;
  }

  /**
   * Returns the corresponding model type if the class of the model type is a
   * categorized entity type. If this is not the case, null is returned and a
   * corresponding error is written to the response.
   */
  static ModelType forceCategorizedTypeOf(
    ProtoType type, StreamObserver<?> resp) {

    var modelType = In.modelTypeOf(type);
    var modelClass = modelType != null
      ? modelType.getModelClass()
      : null;
    if (modelType == null
        || modelClass == null
        || !RootEntity.class.isAssignableFrom(modelClass)) {
      Response.invalidArg(resp, "Not a categorized type: " + modelType);
      return null;
    }
    return modelType;
  }

  static ProtoDataSet.Builder toDataSet(IDatabase db, RefEntity e) {
    var ds = ProtoDataSet.newBuilder();
    var conf = WriterConfig.of(db);

    if (e instanceof Actor)
      return ds.setActor(new ActorWriter(conf)
        .write((Actor) e));

    if (e instanceof Category)
      return ds.setCategory(new CategoryWriter(conf)
        .write((Category) e));

    if (e instanceof Currency)
      return ds.setCurrency(new CurrencyWriter(conf)
        .write((Currency) e));

    if (e instanceof DQSystem)
      return ds.setDqSystem(new DQSystemWriter(conf)
        .write((DQSystem) e));

    if (e instanceof Flow)
      return ds.setFlow(new FlowWriter(conf)
        .write((Flow) e));

    if (e instanceof FlowProperty)
      return ds.setFlowProperty(new FlowPropertyWriter(conf)
        .write((FlowProperty) e));

    if (e instanceof ImpactCategory)
      return ds.setImpactCategory(new ImpactCategoryWriter(conf)
        .write((ImpactCategory) e));

    if (e instanceof ImpactMethod)
      return ds.setImpactMethod(new ImpactMethodWriter(conf)
        .write((ImpactMethod) e));

    if (e instanceof Location)
      return ds.setLocation(new LocationWriter(conf)
        .write((Location) e));

    if (e instanceof Parameter)
      return ds.setParameter(new ParameterWriter(conf)
        .write((Parameter) e));

    if (e instanceof Process)
      return ds.setProcess(new ProcessWriter(conf)
        .write((Process) e));

    if (e instanceof ProductSystem)
      return ds.setProductSystem(new ProductSystemWriter(conf)
        .write((ProductSystem) e));

    if (e instanceof Project)
      return ds.setProject(new ProjectWriter(conf)
        .write((Project) e));

    if (e instanceof SocialIndicator)
      return ds.setSocialIndicator(new SocialIndicatorWriter(conf)
        .write((SocialIndicator) e));

    if (e instanceof Source)
      return ds.setSource(new SourceWriter(conf)
        .write((Source) e));

    if (e instanceof UnitGroup)
      return ds.setUnitGroup(new UnitGroupWriter(conf)
        .write((UnitGroup) e));

    return ds;
  }

  static ProtoReader readerOf(ProtoDataSet dataSet) {
    var store = InMemoryProtoStore.create();
    if (dataSet == null)
      return store;
    if (dataSet.hasActor()) {
      store.putActor(dataSet.getActor());
    } else if (dataSet.hasCategory()) {
      store.putCategory(dataSet.getCategory());
    } else if (dataSet.hasCurrency()) {
      store.putCurrency(dataSet.getCurrency());
    } else if (dataSet.hasDqSystem()) {
      store.putDQSystem(dataSet.getDqSystem());
    } else if (dataSet.hasFlow()) {
      store.putFlow(dataSet.getFlow());
    } else if (dataSet.hasFlowProperty()) {
      store.putFlowProperty(dataSet.getFlowProperty());
    } else if (dataSet.hasImpactCategory()) {
      store.putImpactCategory(dataSet.getImpactCategory());
    } else if (dataSet.hasImpactMethod()) {
      store.putImpactMethod(dataSet.getImpactMethod());
    } else if (dataSet.hasLocation()) {
      store.putLocation(dataSet.getLocation());
    } else if (dataSet.hasParameter()) {
      store.putParameter(dataSet.getParameter());
    } else if (dataSet.hasProcess()) {
      store.putProcess(dataSet.getProcess());
    } else if (dataSet.hasProductSystem()) {
      store.putProductSystem(dataSet.getProductSystem());
    } else if (dataSet.hasProject()) {
      store.putProject(dataSet.getProject());
    } else if (dataSet.hasSocialIndicator()) {
      store.putSocialIndicator(dataSet.getSocialIndicator());
    } else if (dataSet.hasSource()) {
      store.putSource(dataSet.getSource());
    } else if (dataSet.hasUnitGroup()) {
      store.putUnitGroup(dataSet.getUnitGroup());
    }
    return store;
  }


  static <T extends RefEntity> ModelQuery<T> model(
		IDatabase db, Class<T> type) {
    return new ModelQuery<>(db, type);
  }

  static class ModelQuery<T extends RefEntity> {

    private final IDatabase db;
    private final Class<T> type;

    private String id;
    private String name;
    private StreamObserver<?> errorResponse;

    private ModelQuery(IDatabase db, Class<T> type) {
      this.db = Objects.requireNonNull(db);
      this.type = Objects.requireNonNull(type);
    }

    ModelQuery<T> forRef(ProtoRef ref) {
      if (ref == null)
        return this;
      return this.forId(ref.getId())
        .forName(ref.getName());
    }

    ModelQuery<T> forName(String name) {
      this.name = Strings.nullIfEmpty(name);
      return this;
    }

    ModelQuery<T> forId(String id) {
      this.id = Strings.nullIfEmpty(id);
      return this;
    }

    ModelQuery<T> reportErrorsOn(StreamObserver<?> resp) {
      this.errorResponse = resp;
      return this;
    }

    Optional<T> get() {

      if (Strings.notEmpty(id)) {
        var e = db.get(type, id);
        if (e == null && errorResponse != null) {
          Response.notFound(errorResponse,
            "Could not find " + type + " with ID=" + id);
        }
        return Optional.ofNullable(e);
      }

      if (Strings.nullOrEmpty(name)) {
        if (errorResponse != null) {
          Response.invalidArg(errorResponse, "An id or name is required");
        }
        return Optional.empty();
      }

      var e = db.getForName(type, name);
      if (e == null) {
        Response.notFound(errorResponse,
          "Could not find " + type + " with name=" + name);
      }
      return Optional.ofNullable(e);
    }
  }
}
