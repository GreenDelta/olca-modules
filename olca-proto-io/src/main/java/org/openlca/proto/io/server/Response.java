package org.openlca.proto.io.server;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

final class Response {

  private Response() {
  }

  static void invalidArg(StreamObserver<?> resp, String error) {
    resp.onError(Status.INVALID_ARGUMENT.withDescription(error).asException());
  }

  static void notFound(StreamObserver<?> resp, String error) {
    resp.onError(Status.NOT_FOUND.withDescription(error).asException());
  }

  static void serverError(StreamObserver<?> resp, String error) {
    resp.onError(Status.INTERNAL.withDescription(error).asException());
  }

  static void ok(StreamObserver<Empty> resp) {
    resp.onNext(Empty.newBuilder().build());
    resp.onCompleted();
  }
}
