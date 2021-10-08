package org.openlca.proto.io.server;

import org.openlca.core.database.IDatabase;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.proto.ProtoRef;
import org.openlca.proto.grpc.DataUpdateServiceGrpc;
import org.openlca.proto.grpc.DeleteRequest;
import org.openlca.proto.grpc.ProtoDataSet;
import org.openlca.proto.io.input.ImportStatus;
import org.openlca.proto.io.input.ProtoImport;
import org.openlca.proto.io.output.Refs;
import org.openlca.util.Strings;

import com.google.protobuf.Empty;

import io.grpc.stub.StreamObserver;

public class DataUpdateService extends
  DataUpdateServiceGrpc.DataUpdateServiceImplBase {

  private final IDatabase db;

  DataUpdateService(IDatabase db) {
    this.db = db;
  }

  @Override
  public void delete(DeleteRequest req, StreamObserver<Empty> resp) {

    var modelType = DataUtil.forceCategorizedTypeOf(req.getType(), resp);
    if (modelType == null)
      return;
    var id = req.getId();
    if (Strings.nullOrEmpty(id)) {
      Response.invalidArg(resp, "A data set ID is required");
      return;
    }

    var model = db.get(modelType.getModelClass(), id);
    if (model == null) {
      Response.notFound(resp,
        "A data set of type " + modelType + " with id=" + id
        + " does not exist");
      return;
    }
    db.delete(model);
    Response.ok(resp);
  }

  @Override
  public void put(ProtoDataSet dataSet, StreamObserver<ProtoRef> resp) {
    var statusRef = new ImportStatus[1];
    new ProtoImport(DataUtil.readerOf(dataSet), db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .withStatusHandler(s -> statusRef[0] = s)
      .run();
    var status = statusRef[0];

    if (status == null) {
      Response.invalidArg(resp, "No model in data set found");
      return;
    }
    if (status.isError()) {
      Response.serverError(resp, "Import error: " + status.error());
      return;
    }

    var ref = Refs.refOf(status.model());
    resp.onNext(ref.build());
    resp.onCompleted();
  }
}
