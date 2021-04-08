package org.openlca.proto.server;

import io.grpc.stub.StreamObserver;
import org.openlca.proto.generated.Services;
import org.openlca.proto.generated.Status;

final class Response {

  private Response() {
  }

  static void error(StreamObserver<Status> resp, String message) {
    var status = Status.newBuilder()
      .setOk(false)
      .setError(message == null ? "error" : message)
      .build();
    resp.onNext(status);
    resp.onCompleted();
  }

  static void ok(StreamObserver<Status> resp) {
    var status = Status.newBuilder()
      .setOk(true)
      .build();
    resp.onNext(status);
    resp.onCompleted();
  }
}
