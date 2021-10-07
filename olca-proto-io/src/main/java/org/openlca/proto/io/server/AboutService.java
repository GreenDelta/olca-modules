package org.openlca.proto.io.server;

import java.util.Objects;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.openlca.core.database.IDatabase;
import org.openlca.proto.grpc.AboutResponse;
import org.openlca.proto.grpc.AboutServiceGrpc;
import org.openlca.util.Strings;

public class AboutService extends AboutServiceGrpc.AboutServiceImplBase {

  public static final int VERSION = 1;
  public static final int MIN_SUPPORTED_VERSION = 1;
  private final IDatabase db;

  public AboutService(IDatabase db) {
    this.db = Objects.requireNonNull(db);
  }

  @Override
  public void about(Empty req, StreamObserver<AboutResponse> resp) {
    var about = AboutResponse.newBuilder()
      .setVersion(VERSION)
      .setMinSupportedVersion(MIN_SUPPORTED_VERSION)
      .setDatabase(Strings.orEmpty(db.getName()))
      .build();
    resp.onNext(about);
    resp.onCompleted();
  }
}
