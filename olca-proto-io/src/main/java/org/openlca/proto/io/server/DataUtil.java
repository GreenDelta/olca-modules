package org.openlca.proto.io.server;

import java.util.Objects;
import java.util.Optional;

import org.openlca.commons.Strings;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Epd;
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
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.proto.ProtoRef;
import org.openlca.proto.ProtoType;
import org.openlca.proto.grpc.ProtoDataSet;
import org.openlca.proto.io.input.In;
import org.openlca.proto.io.output.ActorWriter;
import org.openlca.proto.io.output.CurrencyWriter;
import org.openlca.proto.io.output.DQSystemWriter;
import org.openlca.proto.io.output.EpdWriter;
import org.openlca.proto.io.output.FlowPropertyWriter;
import org.openlca.proto.io.output.FlowWriter;
import org.openlca.proto.io.output.ImpactCategoryWriter;
import org.openlca.proto.io.output.ImpactMethodWriter;
import org.openlca.proto.io.output.LocationWriter;
import org.openlca.proto.io.output.ParameterWriter;
import org.openlca.proto.io.output.ProcessWriter;
import org.openlca.proto.io.output.ProductSystemWriter;
import org.openlca.proto.io.output.ProjectWriter;
import org.openlca.proto.io.output.ResultWriter;
import org.openlca.proto.io.output.SocialIndicatorWriter;
import org.openlca.proto.io.output.SourceWriter;
import org.openlca.proto.io.output.UnitGroupWriter;
import org.openlca.proto.io.output.WriterConfig;

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

    if (e instanceof Actor actor)
      return ds.setActor(new ActorWriter().write(actor));

    if (e instanceof Currency currency)
      return ds.setCurrency(new CurrencyWriter(conf).write(currency));

    if (e instanceof DQSystem dqs)
      return ds.setDqSystem(new DQSystemWriter(conf) .write(dqs));

    if (e instanceof Flow flow)
      return ds.setFlow(new FlowWriter(conf).write(flow));

    if (e instanceof FlowProperty prop)
      return ds.setFlowProperty(new FlowPropertyWriter(conf).write(prop));

    if (e instanceof ImpactCategory imp)
      return ds.setImpactCategory(new ImpactCategoryWriter(conf).write(imp));

    if (e instanceof ImpactMethod m)
      return ds.setImpactMethod(new ImpactMethodWriter(conf).write(m));

    if (e instanceof Location loc)
      return ds.setLocation(new LocationWriter().write(loc));

    if (e instanceof Parameter param)
      return ds.setParameter(new ParameterWriter().write(param));

    if (e instanceof Process proc)
      return ds.setProcess(new ProcessWriter(conf).write(proc));

    if (e instanceof ProductSystem sys)
      return ds.setProductSystem(new ProductSystemWriter(conf).write(sys));

    if (e instanceof Project proj)
      return ds.setProject(new ProjectWriter(conf).write(proj));

    if (e instanceof SocialIndicator ind)
      return ds.setSocialIndicator(new SocialIndicatorWriter(conf).write(ind));

    if (e instanceof Source s)
      return ds.setSource(new SourceWriter().write(s));

    if (e instanceof UnitGroup group)
      return ds.setUnitGroup(new UnitGroupWriter(conf).write(group));

		if (e instanceof Epd epd)
			return ds.setEpd(new EpdWriter(conf).write(epd));

		if (e instanceof Result r)
			return ds.setResult(new ResultWriter(conf).write(r));

    return ds;
  }

  static <T extends RootEntity> ModelQuery<T> model(
		IDatabase db, Class<T> type) {
    return new ModelQuery<>(db, type);
  }

  static class ModelQuery<T extends RootEntity> {

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
      this.name = Strings.nullIfBlank(name);
      return this;
    }

    ModelQuery<T> forId(String id) {
      this.id = Strings.nullIfBlank(id);
      return this;
    }

    ModelQuery<T> reportErrorsOn(StreamObserver<?> resp) {
      this.errorResponse = resp;
      return this;
    }

    Optional<T> get() {

      if (Strings.isNotBlank(id)) {
        var e = db.get(type, id);
        if (e == null && errorResponse != null) {
          Response.notFound(errorResponse,
            "Could not find " + type + " with ID=" + id);
        }
        return Optional.ofNullable(e);
      }

      if (Strings.isBlank(name)) {
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
