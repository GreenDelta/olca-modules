package org.openlca.proto.server;

import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.RootEntity;
import org.openlca.proto.generated.Proto;
import org.openlca.proto.generated.data.DataFetchServiceGrpc;
import org.openlca.proto.generated.data.DataSet;
import org.openlca.proto.generated.data.FindRequest;
import org.openlca.proto.generated.data.GetRequest;
import org.openlca.proto.input.In;
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

      if (model == null) {
        resp.onNext(DataSet.newBuilder().build());
      } else {
        resp.onNext(DataUtil.toDataSet(db, model).build());
      }
      resp.onCompleted();
    };

    var id = req.getId();
    if (!Strings.notEmpty(id)) {
      onSuccess.accept(db.get(type, id));
      return;
    }
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
