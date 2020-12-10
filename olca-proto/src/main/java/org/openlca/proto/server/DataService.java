package org.openlca.proto.server;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.grpc.stub.StreamObserver;
import org.openlca.core.database.ActorDao;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.database.SocialIndicatorDao;
import org.openlca.core.database.SourceDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.matrix.cache.ProcessTable;
import org.openlca.core.model.Actor;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.proto.MemStore;
import org.openlca.proto.Proto;
import org.openlca.proto.input.CategoryImport;
import org.openlca.proto.input.ProtoImport;
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
import org.openlca.proto.output.Out;
import org.openlca.proto.output.SocialIndicatorWriter;
import org.openlca.proto.output.SourceWriter;
import org.openlca.proto.output.UnitGroupWriter;
import org.openlca.proto.output.WriterConfig;
import org.openlca.proto.services.DataServiceGrpc;
import org.openlca.proto.services.Services;
import org.openlca.util.CategoryPathBuilder;
import org.openlca.util.Strings;

class DataService extends DataServiceGrpc.DataServiceImplBase {

  private final IDatabase db;

  DataService(IDatabase db) {
    this.db = db;
  }

  @Override
  public void delete(Proto.Ref req, StreamObserver<Services.Status> resp) {
    var type = Arrays.stream(ModelType.values())
      .map(ModelType::getModelClass)
      .filter(Objects::nonNull)
      .filter(clazz -> clazz.getSimpleName().equals(req.getType()))
      .findAny()
      .orElse(null);

    if (type == null) {
      Response.error(resp, "Unknown model type: " + req.getType());
      return;
    }

    if (!CategorizedEntity.class.isAssignableFrom(type)) {
      Response.error(resp, req.getType()
        + " is not a standalone entity");
      return;
    }

    var entity = db.get(type, req.getId());
    if (entity == null) {
      Response.error(resp, "A " + req.getType()
        + " with id=" + req.getId() + " does not exist");
      return;
    }

    db.delete(entity);
    Response.ok(resp);
  }

  @Override
  public void getActors(Services.Empty req, StreamObserver<Proto.Actor> resp) {
    var writer = new ActorWriter(WriterConfig.of(db));
    var dao = new ActorDao(db);
    dao.getDescriptors()
      .stream()
      .map(d -> dao.getForId(d.id))
      .map(writer::write)
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void getActor(Proto.Ref req, StreamObserver<Services.ActorStatus> resp) {
    Consumer<String> onError = error -> {
      var status = Services.ActorStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    Consumer<Actor> onSuccess = actor -> {
      var proto = new ActorWriter(WriterConfig.of(db))
        .write(actor);
      var status = Services.ActorStatus.newBuilder()
        .setOk(true)
        .setActor(proto)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    handleGetOf(Actor.class, req.getId(), req::getName, onSuccess, onError);
  }

  @Override
  public void putActor(Proto.Actor req, StreamObserver<Services.RefStatus> resp) {
    var store = new MemStore();
    store.putActor(req);
    new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .run();
    resp.onNext(importStatusOf(Actor.class, req.getId()));
    resp.onCompleted();
  }

  @Override
  public void getCategories(Services.Empty _req, StreamObserver<Proto.Category> resp) {
    var writer = new CategoryWriter(WriterConfig.of(db));
    var dao = new CategoryDao(db);
    dao.getDescriptors()
      .stream()
      .map(d -> dao.getForId(d.id))
      .map(writer::write)
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void getCategory(Proto.Ref req, StreamObserver<Services.CategoryStatus> resp) {
    Consumer<String> onError = error -> {
      var status = Services.CategoryStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    Consumer<Category> onSuccess = category -> {
      var proto = new CategoryWriter(WriterConfig.of(db))
        .write(category);
      var status = Services.CategoryStatus.newBuilder()
        .setOk(true)
        .setCategory(proto)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    handleGetOf(Category.class, req.getId(), req::getName, onSuccess, onError);
  }

  @Override
  public void putCategory(Proto.Category req, StreamObserver<Services.RefStatus> resp) {
    // note that categories behave a bit differently when inserted into a
    // database: their reference IDs may change as they are calculated from
    // the respective category paths
    var store = new MemStore();
    store.putCategory(req);
    var imp = new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS);
    var category = new CategoryImport(imp).of(req.getId());
    var status = Services.RefStatus.newBuilder();
    if (category == null) {
      status.setOk(false)
        .setError("Import of category id=" + req.getId() + " failed");
    } else {
      status.setOk(true)
        .setRef(Out.refOf(category));
    }
    resp.onNext(status.build());
    resp.onCompleted();
  }

  @Override
  public void getCurrencies(Services.Empty _req, StreamObserver<Proto.Currency> resp) {
    var writer = new CurrencyWriter(WriterConfig.of(db));
    var dao = new CurrencyDao(db);
    dao.getDescriptors()
      .stream()
      .map(d -> dao.getForId(d.id))
      .map(writer::write)
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void getCurrency(Proto.Ref req, StreamObserver<Services.CurrencyStatus> resp) {
    Consumer<String> onError = error -> {
      var status = Services.CurrencyStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    Consumer<Currency> onSuccess = currency -> {
      var proto = new CurrencyWriter(WriterConfig.of(db))
        .write(currency);
      var status = Services.CurrencyStatus.newBuilder()
        .setOk(true)
        .setCurrency(proto)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    handleGetOf(Currency.class, req.getId(), req::getName, onSuccess, onError);
  }

  @Override
  public void putCurrency(Proto.Currency req, StreamObserver<Services.RefStatus> resp) {
    var store = new MemStore();
    store.putCurrency(req);
    new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .run();
    resp.onNext(importStatusOf(Currency.class, req.getId()));
    resp.onCompleted();
  }

  @Override
  public void getDQSystems(Services.Empty _req, StreamObserver<Proto.DQSystem> resp) {
    var writer = new DQSystemWriter(WriterConfig.of(db));
    var dao = new DQSystemDao(db);
    dao.getDescriptors()
      .stream()
      .map(d -> dao.getForId(d.id))
      .map(writer::write)
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void getDQSystem(Proto.Ref req, StreamObserver<Services.DQSystemStatus> resp) {
    Consumer<String> onError = error -> {
      var status = Services.DQSystemStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    Consumer<DQSystem> onSuccess = dqSystem -> {
      var proto = new DQSystemWriter(WriterConfig.of(db))
        .write(dqSystem);
      var status = Services.DQSystemStatus.newBuilder()
        .setOk(true)
        .setDqSystem(proto)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    handleGetOf(DQSystem.class, req.getId(), req::getName, onSuccess, onError);
  }

  @Override
  public void putDQSystem(Proto.DQSystem req, StreamObserver<Services.RefStatus> resp) {
    var store = new MemStore();
    store.putDQSystem(req);
    new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .run();
    resp.onNext(importStatusOf(DQSystem.class, req.getId()));
    resp.onCompleted();
  }

  @Override
  public void getFlows(Services.Empty _req, StreamObserver<Proto.Flow> resp) {
    var writer = new FlowWriter(WriterConfig.of(db));
    var dao = new FlowDao(db);
    dao.getDescriptors()
      .stream()
      .map(d -> dao.getForId(d.id))
      .map(writer::write)
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void getFlow(Proto.Ref req, StreamObserver<Services.FlowStatus> resp) {
    Consumer<String> onError = error -> {
      var status = Services.FlowStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    Consumer<Flow> onSuccess = flow -> {
      var proto = new FlowWriter(WriterConfig.of(db))
        .write(flow);
      var status = Services.FlowStatus.newBuilder()
        .setOk(true)
        .setFlow(proto)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    handleGetOf(Flow.class, req.getId(), req::getName, onSuccess, onError);
  }

  @Override
  public void putFlow(Proto.Flow req, StreamObserver<Services.RefStatus> resp) {
    var store = new MemStore();
    store.putFlow(req);
    new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .run();
    resp.onNext(importStatusOf(Flow.class, req.getId()));
    resp.onCompleted();
  }

  @Override
  public void getFlowProperties(Services.Empty _req, StreamObserver<Proto.FlowProperty> resp) {
    var writer = new FlowPropertyWriter(WriterConfig.of(db));
    var dao = new FlowPropertyDao(db);
    dao.getDescriptors()
      .stream()
      .map(d -> dao.getForId(d.id))
      .map(writer::write)
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void getFlowProperty(Proto.Ref req, StreamObserver<Services.FlowPropertyStatus>
    resp) {
    Consumer<String> onError = error -> {
      var status = Services.FlowPropertyStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    Consumer<FlowProperty> onSuccess = property -> {
      var proto = new FlowPropertyWriter(WriterConfig.of(db))
        .write(property);
      var status = Services.FlowPropertyStatus.newBuilder()
        .setOk(true)
        .setFlowProperty(proto)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    handleGetOf(FlowProperty.class, req.getId(), req::getName, onSuccess, onError);
  }

  @Override
  public void putFlowProperty(Proto.FlowProperty req, StreamObserver<Services.RefStatus> resp) {
    var store = new MemStore();
    store.putFlowProperty(req);
    new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .run();
    resp.onNext(importStatusOf(FlowProperty.class, req.getId()));
    resp.onCompleted();
  }

  @Override
  public void getImpactCategories(Services.Empty _req, StreamObserver<Proto.ImpactCategory> resp) {
    var writer = new ImpactCategoryWriter(WriterConfig.of(db));
    var dao = new ImpactCategoryDao(db);
    dao.getDescriptors()
      .stream()
      .map(d -> dao.getForId(d.id))
      .map(writer::write)
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void getImpactCategory(Proto.Ref req, StreamObserver<Services.ImpactCategoryStatus> resp) {
    Consumer<String> onError = error -> {
      var status = Services.ImpactCategoryStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    Consumer<ImpactCategory> onSuccess = impact -> {
      var proto = new ImpactCategoryWriter(WriterConfig.of(db))
        .write(impact);
      var status = Services.ImpactCategoryStatus.newBuilder()
        .setOk(true)
        .setImpactCategory(proto)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    handleGetOf(ImpactCategory.class, req.getId(), req::getName, onSuccess, onError);
  }

  @Override
  public void putImpactCategory(Proto.ImpactCategory req, StreamObserver<Services.RefStatus> resp) {
    var store = new MemStore();
    store.putImpactCategory(req);
    new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .run();
    resp.onNext(importStatusOf(ImpactCategory.class, req.getId()));
    resp.onCompleted();
  }

  @Override
  public void getImpactMethods(Services.Empty _req, StreamObserver<Proto.ImpactMethod> resp) {
    var writer = new ImpactMethodWriter(WriterConfig.of(db));
    var dao = new ImpactMethodDao(db);
    dao.getDescriptors()
      .stream()
      .map(d -> dao.getForId(d.id))
      .map(writer::write)
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void getImpactMethod(Proto.Ref req, StreamObserver<Services.ImpactMethodStatus>
    resp) {
    Consumer<String> onError = error -> {
      var status = Services.ImpactMethodStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    Consumer<ImpactMethod> onSuccess = method -> {
      var proto = new ImpactMethodWriter(WriterConfig.of(db))
        .write(method);
      var status = Services.ImpactMethodStatus.newBuilder()
        .setOk(true)
        .setImpactMethod(proto)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    handleGetOf(ImpactMethod.class, req.getId(), req::getName, onSuccess, onError);
  }

  @Override
  public void putImpactMethod(Proto.ImpactMethod req, StreamObserver<Services.RefStatus> resp) {
    var store = new MemStore();
    store.putImpactMethod(req);
    new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .run();
    resp.onNext(importStatusOf(ImpactMethod.class, req.getId()));
    resp.onCompleted();
  }

  @Override
  public void getLocations(Services.Empty _req, StreamObserver<Proto.Location> resp) {
    var writer = new LocationWriter(WriterConfig.of(db));
    var dao = new LocationDao(db);
    dao.getDescriptors()
      .stream()
      .map(d -> dao.getForId(d.id))
      .map(writer::write)
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void getLocation(Proto.Ref req, StreamObserver<Services.LocationStatus> resp) {
    Consumer<String> onError = error -> {
      var status = Services.LocationStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    Consumer<Location> onSuccess = location -> {
      var proto = new LocationWriter(WriterConfig.of(db))
        .write(location);
      var status = Services.LocationStatus.newBuilder()
        .setOk(true)
        .setLocation(proto)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    handleGetOf(Location.class, req.getId(), req::getName, onSuccess, onError);
  }

  @Override
  public void putLocation(Proto.Location req, StreamObserver<Services.RefStatus> resp) {
    var store = new MemStore();
    store.putLocation(req);
    new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .run();
    resp.onNext(importStatusOf(Location.class, req.getId()));
    resp.onCompleted();
  }

  @Override
  public void getParameters(Services.Empty _req, StreamObserver<Proto.Parameter> resp) {
    var writer = new ParameterWriter(WriterConfig.of(db));
    var dao = new ParameterDao(db);
    dao.getDescriptors()
      .stream()
      .map(d -> dao.getForId(d.id))
      .map(writer::write)
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void getParameter(Proto.Ref req, StreamObserver<Services.ParameterStatus> resp) {
    Consumer<String> onError = error -> {
      var status = Services.ParameterStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    Consumer<Parameter> onSuccess = parameter -> {
      var proto = new ParameterWriter(WriterConfig.of(db))
        .write(parameter);
      var status = Services.ParameterStatus.newBuilder()
        .setOk(true)
        .setParameter(proto)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    handleGetOf(Parameter.class, req.getId(), req::getName, onSuccess, onError);
  }

  @Override
  public void putParameter(Proto.Parameter req, StreamObserver<Services.RefStatus> resp) {
    var store = new MemStore();
    store.putParameter(req);
    new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .run();
    resp.onNext(importStatusOf(Parameter.class, req.getId()));
    resp.onCompleted();
  }

  @Override
  public void getProcesses(Services.Empty _req, StreamObserver<Proto.Process> resp) {
    var writer = new ProcessWriter(WriterConfig.of(db));
    var dao = new ProcessDao(db);
    dao.getDescriptors()
      .stream()
      .map(d -> dao.getForId(d.id))
      .map(writer::write)
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void getProcess(Proto.Ref req, StreamObserver<Services.ProcessStatus> resp) {
    Consumer<String> onError = error -> {
      var status = Services.ProcessStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    Consumer<Process> onSuccess = process -> {
      var proto = new ProcessWriter(WriterConfig.of(db))
        .write(process);
      var status = Services.ProcessStatus.newBuilder()
        .setOk(true)
        .setProcess(proto)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    handleGetOf(Process.class, req.getId(), req::getName, onSuccess, onError);
  }

  @Override
  public void putProcess(Proto.Process req, StreamObserver<Services.RefStatus> resp) {
    var store = new MemStore();
    store.putProcess(req);
    new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .run();
    resp.onNext(importStatusOf(Process.class, req.getId()));
    resp.onCompleted();
  }

  @Override
  public void getProductSystems(Services.Empty _req, StreamObserver<Proto.ProductSystem> resp) {
    var writer = new ProductSystemWriter(WriterConfig.of(db));
    var dao = new ProductSystemDao(db);
    dao.getDescriptors()
      .stream()
      .map(d -> dao.getForId(d.id))
      .map(writer::write)
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void getProductSystem(Proto.Ref req, StreamObserver<Services.ProductSystemStatus> resp) {
    Consumer<String> onError = error -> {
      var status = Services.ProductSystemStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    Consumer<ProductSystem> onSuccess = system -> {
      var proto = new ProductSystemWriter(WriterConfig.of(db))
        .write(system);
      var status = Services.ProductSystemStatus.newBuilder()
        .setOk(true)
        .setProductSystem(proto)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    handleGetOf(ProductSystem.class, req.getId(), req::getName, onSuccess, onError);
  }

  @Override
  public void putProductSystem(Proto.ProductSystem req, StreamObserver<Services.RefStatus> resp) {
    var store = new MemStore();
    store.putProductSystem(req);
    new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .run();
    resp.onNext(importStatusOf(ProductSystem.class, req.getId()));
    resp.onCompleted();
  }

  @Override
  public void getProjects(Services.Empty _req, StreamObserver<Proto.Project> resp) {
    var writer = new ProjectWriter(WriterConfig.of(db));
    var dao = new ProjectDao(db);
    dao.getDescriptors()
      .stream()
      .map(d -> dao.getForId(d.id))
      .map(writer::write)
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void getProject(Proto.Ref req, StreamObserver<Services.ProjectStatus> resp) {
    Consumer<String> onError = error -> {
      var status = Services.ProjectStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    Consumer<Project> onSuccess = project -> {
      var proto = new ProjectWriter(WriterConfig.of(db))
        .write(project);
      var status = Services.ProjectStatus.newBuilder()
        .setOk(true)
        .setProject(proto)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    handleGetOf(Project.class, req.getId(), req::getName, onSuccess, onError);
  }

  @Override
  public void putProject(Proto.Project req, StreamObserver<Services.RefStatus> resp) {
    var store = new MemStore();
    store.putProject(req);
    new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .run();
    resp.onNext(importStatusOf(Project.class, req.getId()));
    resp.onCompleted();
  }

  @Override
  public void getSocialIndicators(Services.Empty _req, StreamObserver<Proto.SocialIndicator> resp) {
    var writer = new SocialIndicatorWriter(WriterConfig.of(db));
    var dao = new SocialIndicatorDao(db);
    dao.getDescriptors()
      .stream()
      .map(d -> dao.getForId(d.id))
      .map(writer::write)
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void getSocialIndicator(Proto.Ref req, StreamObserver<Services.SocialIndicatorStatus> resp) {
    Consumer<String> onError = error -> {
      var status = Services.SocialIndicatorStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    Consumer<SocialIndicator> onSuccess = indicator -> {
      var proto = new SocialIndicatorWriter(WriterConfig.of(db))
        .write(indicator);
      var status = Services.SocialIndicatorStatus.newBuilder()
        .setOk(true)
        .setSocialIndicator(proto)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    handleGetOf(SocialIndicator.class, req.getId(), req::getName, onSuccess, onError);
  }

  @Override
  public void putSocialIndicator(Proto.SocialIndicator req, StreamObserver<Services.RefStatus> resp) {
    var store = new MemStore();
    store.putSocialIndicator(req);
    new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .run();
    resp.onNext(importStatusOf(SocialIndicator.class, req.getId()));
    resp.onCompleted();
  }

  @Override
  public void getSources(Services.Empty _req, StreamObserver<Proto.Source> resp) {
    var writer = new SourceWriter(WriterConfig.of(db));
    var dao = new SourceDao(db);
    dao.getDescriptors()
      .stream()
      .map(d -> dao.getForId(d.id))
      .map(writer::write)
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void getSource(Proto.Ref req, StreamObserver<Services.SourceStatus> resp) {
    Consumer<String> onError = error -> {
      var status = Services.SourceStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    Consumer<Source> onSuccess = source -> {
      var proto = new SourceWriter(WriterConfig.of(db))
        .write(source);
      var status = Services.SourceStatus.newBuilder()
        .setOk(true)
        .setSource(proto)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    handleGetOf(Source.class, req.getId(), req::getName, onSuccess, onError);
  }

  @Override
  public void putSource(Proto.Source req, StreamObserver<Services.RefStatus> resp) {
    var store = new MemStore();
    store.putSource(req);
    new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .run();
    resp.onNext(importStatusOf(Source.class, req.getId()));
    resp.onCompleted();
  }

  @Override
  public void getUnitGroups(Services.Empty _req, StreamObserver<Proto.UnitGroup> resp) {
    var writer = new UnitGroupWriter(WriterConfig.of(db));
    var dao = new UnitGroupDao(db);
    dao.getDescriptors()
      .stream()
      .map(d -> dao.getForId(d.id))
      .map(writer::write)
      .forEach(resp::onNext);
    resp.onCompleted();
  }

  @Override
  public void getUnitGroup(Proto.Ref req, StreamObserver<Services.UnitGroupStatus> resp) {
    Consumer<String> onError = error -> {
      var status = Services.UnitGroupStatus.newBuilder()
        .setOk(false)
        .setError(error)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    Consumer<UnitGroup> onSuccess = group -> {
      var proto = new UnitGroupWriter(WriterConfig.of(db))
        .write(group);
      var status = Services.UnitGroupStatus.newBuilder()
        .setOk(true)
        .setUnitGroup(proto)
        .build();
      resp.onNext(status);
      resp.onCompleted();
    };

    handleGetOf(UnitGroup.class, req.getId(), req::getName, onSuccess, onError);
  }

  @Override
  public void putUnitGroup(Proto.UnitGroup req, StreamObserver<Services.RefStatus> resp) {
    var store = new MemStore();
    store.putUnitGroup(req);
    new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .run();
    resp.onNext(importStatusOf(UnitGroup.class, req.getId()));
    resp.onCompleted();
  }

  @Override
  public void getProvidersFor(Proto.FlowRef req, StreamObserver<Proto.ProcessRef> resp) {
    Consumer<Flow> onFlowExists = flow -> {
      if (flow.flowType == FlowType.ELEMENTARY_FLOW) {
        resp.onCompleted();
        return;
      }

      var locationCodes = new LocationDao(db).getCodes();
      var categories = new CategoryPathBuilder(db);

      ProcessTable.create(db)
        .getProviders(flow.id)
        .stream()
        .map(p -> p.process)
        .filter(p -> p instanceof ProcessDescriptor)
        .map(p -> (ProcessDescriptor) p)
        .forEach(p -> {

          var ref = Proto.ProcessRef.newBuilder()
            .setId(Strings.orEmpty(p.refId))
            .setName(Strings.orEmpty(p.name))
            .setDescription(Strings.orEmpty(p.description))
            .setVersion(Version.asString(p.version))
            .setType("Process");

          if (p.lastChange != 0) {
            var instant = Instant.ofEpochMilli(p.lastChange);
            ref.setLastChange(instant.toString());
          }

          if (p.category != null) {
            var path = categories.build(p.category);
            if (path != null) {
              Arrays.stream(path.split("/"))
                .forEach(ref::addCategoryPath);
            }
          }

          if (p.location != null) {
            var code = locationCodes.get(p.location);
            if (code != null) {
              ref.setLocation(code);
            }
          }

          ref.setProcessType(
            p.processType == ProcessType.LCI_RESULT
              ? Proto.ProcessType.LCI_RESULT
              : Proto.ProcessType.UNIT_PROCESS);

          resp.onNext(ref.build());

        });

      resp.onCompleted();
    };

    Consumer<String> onFlowMissing = error -> resp.onCompleted();

    handleGetOf(Flow.class, req.getId(), req::getName, onFlowExists, onFlowMissing);
  }

  private Services.RefStatus importStatusOf(
    Class<? extends RootEntity> type, String id) {
    var e = db.get(type, id);
    if (e == null) {
      return Services.RefStatus.newBuilder()
        .setOk(false)
        .setError("Import of " + type.getSimpleName() + " " + id + " failed")
        .build();
    }
    return Services.RefStatus.newBuilder()
      .setOk(true)
      .setRef(Out.refOf(e))
      .build();
  }

  private <T extends RootEntity> void handleGetOf(
    Class<T> type,
    String id,
    Supplier<String> nameFn,
    Consumer<T> onSuccess,
    Consumer<String> onError) {

    // get by ID
    if (Strings.notEmpty(id)) {
      var e = db.get(type, id);
      if (e != null) {
        onSuccess.accept(e);
      } else {
        onError.accept(
          "An instance of " + type.getSimpleName()
            + " with id='" + id + "' does not exist");
      }
      return;
    }

    // get by name
    var name = nameFn.get();
    if (Strings.nullOrEmpty(name)) {
      onError.accept("An id or name is required");
      return;
    }
    var e = db.forName(type, name);
    if (e != null) {
      onSuccess.accept(e);
    } else {
      onError.accept(
        "An instance of " + type.getSimpleName()
          + " with name='" + name + "' does not exist");
    }
  }
}
