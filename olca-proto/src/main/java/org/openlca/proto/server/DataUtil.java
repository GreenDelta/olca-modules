package org.openlca.proto.server;

import java.util.Objects;
import java.util.Optional;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
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
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.proto.generated.Proto;
import org.openlca.proto.generated.data.DataSet;
import org.openlca.proto.output.ActorWriter;
import org.openlca.proto.output.CategoryWriter;
import org.openlca.proto.output.CurrencyWriter;
import org.openlca.proto.output.DQSystemWriter;
import org.openlca.proto.output.FlowPropertyWriter;
import org.openlca.proto.output.FlowWriter;
import org.openlca.proto.output.ImpactCategoryWriter;
import org.openlca.proto.output.ImpactMethodWriter;
import org.openlca.proto.output.LocationWriter;
import org.openlca.proto.output.ParameterWriter;
import org.openlca.proto.output.ProcessWriter;
import org.openlca.proto.output.ProductSystemWriter;
import org.openlca.proto.output.ProjectWriter;
import org.openlca.proto.output.SocialIndicatorWriter;
import org.openlca.proto.output.SourceWriter;
import org.openlca.proto.output.UnitGroupWriter;
import org.openlca.proto.output.WriterConfig;
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

  static DataSet.Builder toDataSet(IDatabase db, RootEntity e) {
    var ds = DataSet.newBuilder();
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

    ModelQuery<T> forRef(Proto.Ref ref) {
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

      var e = db.forName(type, name);
      if (e == null) {
        Response.notFound(errorResponse,
          "Could not find " + type + " with name=" + name);
      }
      return Optional.ofNullable(e);
    }
  }
}
