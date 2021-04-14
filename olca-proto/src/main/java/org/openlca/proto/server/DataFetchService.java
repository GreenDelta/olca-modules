package org.openlca.proto.server;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.cache.ProcessTable;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.proto.generated.Proto;
import org.openlca.proto.generated.Proto.Ref;
import org.openlca.proto.generated.data.CategoryTree;
import org.openlca.proto.generated.data.DataFetchServiceGrpc;
import org.openlca.proto.generated.data.DataSet;
import org.openlca.proto.generated.data.FindRequest;
import org.openlca.proto.generated.data.GetAllRequest;
import org.openlca.proto.generated.data.GetAllResponse;
import org.openlca.proto.generated.data.GetCategoryContentRequest;
import org.openlca.proto.generated.data.GetCategoryTreeRequest;
import org.openlca.proto.generated.data.GetDescriptorsRequest;
import org.openlca.proto.generated.data.GetRequest;
import org.openlca.proto.generated.data.SearchRequest;
import org.openlca.proto.input.In;
import org.openlca.proto.output.Refs;
import org.openlca.util.Strings;

import io.grpc.stub.StreamObserver;

class DataFetchService extends
  DataFetchServiceGrpc.DataFetchServiceImplBase {

  private final IDatabase db;

  DataFetchService(IDatabase db) {
    this.db = db;
  }

  @Override
  public void get(GetRequest req, StreamObserver<DataSet> resp) {

    var type = forceClassOf(req.getModelType(), resp);
    if (type == null)
      return;

    var id = req.getId();
    if (Strings.nullOrEmpty(id)) {
      Response.invalidArg(resp, "A data set ID is required");
      return;
    }

    var model = db.get(type, id);
    if (model == null) {
      Response.notFound(resp, "No " + type
                              + " with ID=" + id + " exists");
      return;
    }

    resp.onNext(DataUtil.toDataSet(db, model).build());
    resp.onCompleted();
  }

  @Override
  public void find(FindRequest req, StreamObserver<DataSet> resp) {

    var type = forceClassOf(req.getModelType(), resp);
    if (type == null)
      return;

    Consumer<RootEntity> onSuccess = model -> {
      resp.onNext(model == null
        ? DataSet.newBuilder().build()
        : DataUtil.toDataSet(db, model).build());
      resp.onCompleted();
    };

    var id = req.getId();
    if (Strings.notEmpty(id)) {
      onSuccess.accept(db.get(type, id));
      return;
    }

    var name = req.getName();
    onSuccess.accept(Strings.notEmpty(name)
      ? db.forName(type, name)
      : null);
  }

  @Override
  public void getAll(GetAllRequest req, StreamObserver<GetAllResponse> resp) {

    var type = forceClassOf(req.getModelType(), resp);
    if (type == null)
      return;

    var pageSize = req.getPageSize() > 0
      ? req.getPageSize()
      : 100;
    var page = req.getPage() > 0
      ? req.getPage()
      : 1;
    var response = GetAllResponse.newBuilder()
      .setPageSize(pageSize)
      .setPage(page);

    var descriptors = db.allDescriptorsOf(type);
    var totalCount = descriptors.size();
    response.setTotalCount(totalCount);
    var offset = (page - 1) * pageSize;
    if (offset >= totalCount) {
      resp.onNext(response.build());
      resp.onCompleted();
      return;
    }

    var end = Math.min(totalCount, offset + pageSize);
    descriptors.subList(offset, end)
      .stream()
      .map(d -> db.get(type, d.id))
      .map(e -> DataUtil.toDataSet(db, e))
      .forEach(response::addDataSet);
    resp.onNext(response.build());
    resp.onCompleted();
  }

  @Override
  public void getDescriptors(
    GetDescriptorsRequest req, StreamObserver<Proto.Ref> resp) {

    // TODO: allow root entities; factor out in Data(Fetch)Util
    var modelType = forceCategorizedTypeOf(req.getModelType(), resp);
    if (modelType == null)
      return;

    var dao = Daos.root(db, modelType);
    var refData = Refs.dataOf(db);

    // get by id
    var id = req.getId();
    if (Strings.notEmpty(id)) {
      var d = dao.getDescriptorForRefId(id);
      if (d != null) {
        resp.onNext(Refs.refOf(d, refData).build());
      }
      resp.onCompleted();
      return;
    }

    if(!req.hasAttributes()) {
      resp.onCompleted();
      return;
    }

    var stream = dao.getDescriptors().stream();

    // filter by category
    var catId = req.getAttributes().getCategory();
    if (Strings.notEmpty(catId)) {
      stream = stream.filter(
        d -> d instanceof CategorizedDescriptor);

      // "/" identifies the root category or no
      // specific category
      if (catId.equals("/")) {
        stream = stream.filter(d -> {
          var cd = (CategorizedDescriptor) d;
          return cd.category == null;
        });
      } else {
        var category = DataUtil.getCategory(
          db, modelType, catId);
        if (category == null) {
          resp.onCompleted();
          return;
        }
        stream = stream.filter(d -> {
          var cd = (CategorizedDescriptor) d;
          return cd.category != null
                 && cd.category == category.id;
        });
      }
    }

    // filter by name
    var name = req.getAttributes().getName();
    if (Strings.notEmpty(name)) {
      var term = name.trim().toLowerCase();
      stream = stream.filter(d -> {
        if (d.name == null)
          return false;
        var other = d.name.trim().toLowerCase();
        return other.equals(term);
      });
    }

    stream.forEach(d -> resp.onNext(
      Refs.refOf(d, refData).build()));
    resp.onCompleted();
  }

  @Override
  public void search(SearchRequest req, StreamObserver<Proto.Ref> resp) {
    var refData = Refs.dataOf(db);
    Search.of(db, req).run()
      .map(descriptor -> Refs.refOf(descriptor, refData))
      .forEach(ref -> resp.onNext(ref.build()));
    resp.onCompleted();
  }

  @Override
  public void getCategoryContent(
    GetCategoryContentRequest req, StreamObserver<Ref> resp) {

    var modelType = forceCategorizedTypeOf(req.getModelType(), resp);
    if (modelType == null)
      return;

    // find the category
    Optional<Category> category;
    var catID = req.getCategory();
    if (Strings.nullOrEmpty(catID) || catID.equals("/")) {
      category = Optional.empty();
    } else {
      var cat = DataUtil.getCategory(db, modelType, catID);
      if (cat == null) {
        Response.notFound(resp,
          "Category with ID ot path =" + catID + " does not exists");
        return;
      }
      category = Optional.of(cat);
    }

    var dao = Daos.categorized(db, modelType);
    for (var d : dao.getDescriptors(category)) {
      resp.onNext(Refs.refOf(d).build());
    }
    resp.onCompleted();
  }

  @Override
  public void getCategoryTree(
    GetCategoryTreeRequest req, StreamObserver<CategoryTree> resp) {
    var modelType = forceCategorizedTypeOf(req.getModelType(), resp);
    if (modelType == null)
      return;
    var root = CategoryTree.newBuilder();
    root.setModelType(req.getModelType());
    new CategoryDao(db).getRootCategories(modelType)
      .forEach(c -> expand(root, c));
    resp.onNext(root.build());
    resp.onCompleted();
  }

  private void expand(CategoryTree.Builder parent, Category category) {
    var tree = CategoryTree.newBuilder()
      .setModelType(parent.getModelType())
      .setName(Strings.orEmpty(category.name));
    category.childCategories.forEach(c -> expand(tree, c));
    parent.addSubTree(tree);
  }

  private Class<? extends RootEntity> forceClassOf(
    Proto.ModelType type, StreamObserver<?> resp) {
    var modelType = In.modelTypeOf(type);
    if (modelType == null || modelType.getModelClass() == null) {
      Response.invalidArg(resp, "Invalid model type: " + type);
      return null;
    }
    return modelType.getModelClass();
  }

  private ModelType forceCategorizedTypeOf(
    Proto.ModelType type, StreamObserver<?> resp) {
    var modelType = In.modelTypeOf(type);
    var modelClass = modelType != null
      ? modelType.getModelClass()
      : null;
    if (modelType == null
        || modelClass == null
        || !CategorizedEntity.class.isAssignableFrom(modelClass)) {
      Response.invalidArg(resp, "Not a categorized type: " + modelType);
      return null;
    }
    return modelType;
  }

  @Override
  public void getProvidersFor(Proto.Ref req, StreamObserver<Proto.Ref> resp) {
    var flow = DataUtil.model(db, Flow.class)
      .forRef(req)
      .reportErrorsOn(resp)
      .get()
      .orElse(null);
    if (flow == null)
      return;

    if (flow.flowType == FlowType.ELEMENTARY_FLOW) {
      resp.onCompleted();
      return;
    }

    var refData = Refs.dataOf(db);

    ProcessTable.create(db)
      .getProviders(flow.id)
      .stream()
      .map(p -> p.process)
      .filter(p -> p instanceof ProcessDescriptor)
      .map(p -> (ProcessDescriptor) p)
      .forEach(p -> resp.onNext(Refs.refOf(p, refData).build()));

    resp.onCompleted();
  }
}
