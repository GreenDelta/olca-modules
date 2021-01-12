package org.openlca.proto.server;

import java.util.HashMap;
import java.util.Map;

import io.grpc.stub.StreamObserver;
import org.openlca.core.database.IDatabase;
import org.openlca.core.results.SimpleResult;
import org.openlca.proto.generated.ResultServiceGrpc;
import org.openlca.proto.generated.Services;

class ResultService extends ResultServiceGrpc.ResultServiceImplBase {

  private final IDatabase db;
  private final Map<String, SimpleResult> results = new HashMap<>();

  ResultService(IDatabase db) {
    this.db = db;
  }

  @Override
  public void dispose(Services.Result req, StreamObserver<Services.Status> resp) {
    results.remove(req.getId());
    // we always return ok, even when the result does not exist
    resp.onNext(Services.Status
      .newBuilder()
      .setOk(true)
      .build());
    resp.onCompleted();
  }
}
