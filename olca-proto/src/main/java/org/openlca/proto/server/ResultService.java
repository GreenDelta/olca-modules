package org.openlca.proto.server;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Strings;
import io.grpc.stub.StreamObserver;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.proto.Messages;
import org.openlca.proto.generated.Proto;
import org.openlca.proto.generated.ResultServiceGrpc;
import org.openlca.proto.generated.Services;
import org.openlca.util.Pair;

class ResultService extends ResultServiceGrpc.ResultServiceImplBase {

  private final IDatabase db;
  private final Map<String, SimpleResult> results = new HashMap<>();

  ResultService(IDatabase db) {
    this.db = db;
  }

  @Override
  public void calculate(
    Proto.CalculationSetup req, StreamObserver<Services.ResultStatus> resp) {
    var p = setup(req);
    if (p.first == null) {
      var status = Services.ResultStatus.newBuilder()
        .setOk(false)
        .setError(p.second)
        .build();
      resp.onNext(status);
      resp.onCompleted();
      return;
    }

    var setup = p.first;

  }

  private Pair<CalculationSetup, String> setup(Proto.CalculationSetup proto) {
    var system = systemOf(proto);
    if (system == null)
      return Pair.of(null, "Product system or process does not exist");
    var setup = new CalculationSetup(system);

    // add parameter redefinitions
    setup.parameterRedefs.clear();
    var protoRedefs = proto.getParameterRedefsList();
    if (protoRedefs.isEmpty()) {
      system.parameterSets.stream()
        .filter(set -> set.isBaseline)
        .findAny()
        .ifPresent(set -> setup.parameterRedefs.addAll(set.parameters));
    } else {
      for (var protoRedef : protoRedefs) {

      }
    }

    return Pair.of(setup, null);
  }

  private ProductSystem systemOf(Proto.CalculationSetup proto) {
    var refID = proto.getProductSystem().getId();
    if (Strings.isNullOrEmpty(refID))
      return null;
    var system = db.get(ProductSystem.class, refID);
    if (system != null)
      return system;
    var process = db.get(Process.class, refID);
    if (process == null)
      return null;
    system = ProductSystem.of(process);
    system.withoutNetwork = true;
    return system;
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
