package org.openlca.proto.io.server;

import org.openlca.core.database.IDatabase;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.proto.ProtoRef;
import org.openlca.proto.grpc.DataUpdateServiceGrpc;
import org.openlca.proto.grpc.DeleteRequest;
import org.openlca.proto.grpc.ProtoDataSet;
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

		var reader = DataSetReader.of(dataSet);
		var type = reader.getType();
		var id = reader.getId();
		if (type == null || !type.isRoot() || Strings.nullOrEmpty(id)) {
			Response.invalidArg(resp, "No valid data set provided");
			return;
		}

    var model = new ProtoImport(reader, db)
      .setUpdateMode(UpdateMode.ALWAYS)
      .run(type, id);
    if (model == null) {
      Response.serverError(resp, "Failed to import or update data set");
      return;
    }

    var ref = Refs.refOf(model);
    resp.onNext(ref.build());
    resp.onCompleted();
  }
}
