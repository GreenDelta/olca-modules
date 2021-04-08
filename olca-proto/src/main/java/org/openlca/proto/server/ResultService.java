package org.openlca.proto.server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.NwSetDao;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.providers.ResultProviders;
import org.openlca.proto.generated.commons.Status;
import org.openlca.proto.generated.results.Result;
import org.openlca.proto.generated.results.ResultServiceGrpc;
import org.openlca.proto.generated.results.ResultStatus;
import org.openlca.proto.input.In;
import org.openlca.proto.output.Out;
import org.openlca.util.Strings;
import io.grpc.stub.StreamObserver;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.proto.generated.Proto;
import org.openlca.util.Pair;

class ResultService extends ResultServiceGrpc.ResultServiceImplBase {

  private final IDatabase db;
  private final Map<String, FullResult> results = new HashMap<>();

  ResultService(IDatabase db) {
    this.db = db;
  }

  @Override
  public void calculate(
    Proto.CalculationSetup req, StreamObserver<ResultStatus> resp) {
    var p = setup(req);
    if (p.first == null) {
      var status = ResultStatus.newBuilder()
        .setOk(false)
        .setError(p.second)
        .build();
      resp.onNext(status);
      resp.onCompleted();
      return;
    }

    var setup = p.first;
    var data = MatrixData.of(db, setup);
    var provider = ResultProviders.lazyOf(db, data);
    var result = new FullResult(provider);

    var key = UUID.randomUUID().toString();
    results.put(key, result);
    var status = ResultStatus.newBuilder()
      .setOk(true)
      .setResult(Result.newBuilder().setId(key))
      .build();
    resp.onNext(status);
    resp.onCompleted();
  }

  private Pair<CalculationSetup, String> setup(Proto.CalculationSetup proto) {
    var system = systemOf(proto);
    if (system == null)
      return Pair.of(null, "Product system or process does not exist");
    var setup = new CalculationSetup(system);

    // demand value
    if (proto.getAmount() != 0) {
      setup.setAmount(proto.getAmount());
    }

    // flow property
    var qref = system.referenceExchange;
    var propID = proto.getFlowProperty().getId();
    if (Strings.notEmpty(propID)
      && qref != null
      && qref.flow != null) {
      qref.flow.flowPropertyFactors.stream()
        .filter(f -> Strings.nullOrEqual(propID, f.flowProperty.refId))
        .findAny()
        .ifPresent(setup::setFlowPropertyFactor);
    }

    // unit
    var unitID = proto.getUnit().getId();
    var propFac = setup.getFlowPropertyFactor();
    if (Strings.notEmpty(unitID)
      && propFac != null
      && propFac.flowProperty != null
      && propFac.flowProperty.unitGroup != null) {
      var group = propFac.flowProperty.unitGroup;
      group.units.stream()
        .filter(u -> Strings.nullOrEqual(unitID, u.refId))
        .findAny()
        .ifPresent(setup::setUnit);
    }

    // impact method and NW set
    var methodID = proto.getImpactMethod().getId();
    if (Strings.notEmpty(methodID)) {
      setup.impactMethod = new ImpactMethodDao(db)
        .getDescriptorForRefId(methodID);
      var nwID = proto.getNwSet().getId();
      if (Strings.notEmpty(nwID)) {
        setup.nwSet = new NwSetDao(db)
          .getDescriptorForRefId(nwID);
      }
    }

    // other settings
    setup.allocationMethod = In.allocationMethod(proto.getAllocationMethod());
    setup.withCosts = proto.getWithCosts();
    setup.withRegionalization = proto.getWithRegionalization();

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
        var redef = In.parameterRedefOf(protoRedef, db);
        setup.parameterRedefs.add(redef);
      }
    }

    return Pair.of(setup, null);
  }

  private ProductSystem systemOf(Proto.CalculationSetup proto) {
    var refID = proto.getProductSystem().getId();
    if (Strings.nullOrEmpty(refID))
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
  public void getInventory(Result req, StreamObserver<Proto.FlowResult> resp) {

    // get the flow results
    var result = results.get(req.getId());
    if (result == null) {
      resp.onCompleted();
      return;
    }
    var flowResults = result.getTotalFlowResults();
    if (flowResults.isEmpty()) {
      resp.onCompleted();
      return;
    }

    // create the result objects
    var decorator = Descriptors.decorator(db);
    for (var fr : result.getTotalFlowResults()) {
      if (fr.flow == null)
        continue;
      var proto = Proto.FlowResult.newBuilder();
      proto.setFlow(decorator.of(fr.flow));
      proto.setInput(fr.input);
      proto.setValue(fr.value);
      resp.onNext(proto.build());
      if (fr.location != null) {
        proto.setLocation(Out.refOf(fr.location));
      }
    }
    resp.onCompleted();
  }

  @Override
  public void getImpacts(Result req, StreamObserver<Proto.ImpactResult> resp) {

    // get the impact results
    var result = results.get(req.getId());
    if (result == null) {
      resp.onCompleted();
      return;
    }
    var impacts = result.getTotalImpactResults();
    if (impacts.isEmpty()) {
      resp.onCompleted();
      return;
    }

    // create the result data
    for (var impact : impacts) {
      var proto = Proto.ImpactResult.newBuilder();
      proto.setImpactCategory(Out.refOf(impact.impact));
      proto.setValue(impact.value);
      resp.onNext(proto.build());
    }
    resp.onCompleted();
  }

  @Override
  public void dispose(Result req, StreamObserver<Status> resp) {
    results.remove(req.getId());
    // we always return ok, even when the result does not exist
    resp.onNext(Status
      .newBuilder()
      .setOk(true)
      .build());
    resp.onCompleted();
  }
}
