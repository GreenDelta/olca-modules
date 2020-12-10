package org.openlca.proto.server;

import io.grpc.stub.StreamObserver;
import org.openlca.proto.services.Services;

final class Response {

  private Response() {
  }

  static void error(StreamObserver<Services.Status> resp, String message) {
    var status = Services.Status.newBuilder()
      .setOk(false)
      .setError(message == null ? "error" : message)
      .build();
    resp.onNext(status);
    resp.onCompleted();
  }

  static void ok(StreamObserver<Services.Status> resp) {
    var status = Services.Status.newBuilder()
      .setOk(true)
      .build();
    resp.onNext(status);
    resp.onCompleted();
  }
}
