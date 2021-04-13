package org.openlca.proto.server;

import java.util.Optional;
import java.util.function.Consumer;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.RootEntity;
import org.openlca.proto.generated.Proto;
import org.openlca.proto.generated.Proto.Ref;
import org.openlca.proto.generated.data.DataFetchServiceGrpc;
import org.openlca.proto.generated.data.DataSet;
import org.openlca.proto.generated.data.FindRequest;
import org.openlca.proto.generated.data.GetAllRequest;
import org.openlca.proto.generated.data.GetAllResponse;
import org.openlca.proto.generated.data.GetCategoryContentRequest;
import org.openlca.proto.generated.data.GetRequest;
import org.openlca.proto.input.In;
import org.openlca.proto.output.Out;
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
  public void getCategoryContent(
      GetCategoryContentRequest req, StreamObserver<Ref> resp) {

    // check that the request is for a categorized entity type
    var modelType = In.modelTypeOf(req.getModelType());
    var modelClass = modelType != null
        ? modelType.getClass()
        : null;
    if (modelType == null
        || modelClass == null
        || !CategorizedEntity.class.isAssignableFrom(modelClass)) {
      Response.invalidArg(resp, "Not a categorized type: " + modelType);
      return;
    }

    // find the category
    Optional<Category> category;
    var catID = req.getCategory();
    if (Strings.nullOrEmpty(catID) || catID.equals("/")) {
      category = Optional.empty();
    } else {
      var dao = new CategoryDao(db);
      var cat = dao.getForRefId(catID);
      if (cat == null) {
        cat = dao.getForPath(modelType, catID);
      }
      if (cat == null) {
        Response.notFound(resp,
            "Category with ID ot path =" + catID + " does not exists");
        return;
      }
      category = Optional.of(cat);
    }

    var dao = Daos.categorized(db, modelType);
    for (var d : dao.getDescriptors(category)) {
      resp.onNext(Out.refOf(d).build());
    }
    resp.onCompleted();
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

}
