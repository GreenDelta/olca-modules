package org.openlca.proto.server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.protobuf.Empty;
import io.grpc.Status;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.NwSetDao;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.providers.ResultProviders;
import org.openlca.proto.generated.results.ImpactFactorRequest;
import org.openlca.proto.generated.results.ImpactFactorResponse;
import org.openlca.proto.generated.results.Result;
import org.openlca.proto.generated.results.ResultServiceGrpc;
import org.openlca.proto.generated.results.ResultsProto;
import org.openlca.proto.generated.results.TechFlowContributionRequest;
import org.openlca.proto.input.In;
import org.openlca.proto.output.Refs;
import org.openlca.util.Strings;
import io.grpc.stub.StreamObserver;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.proto.generated.Proto;
import org.openlca.util.Pair;

class ResultService extends ResultServiceGrpc.ResultServiceImplBase {

  final IDatabase db;
  final Map<String, FullResult> results = new HashMap<>();

  ResultService(IDatabase db) {
    this.db = db;
  }

  @Override
  public void calculate(
    Proto.CalculationSetup req, StreamObserver<Result> resp) {
    var p = setup(req);
    if (p.first == null) {
      resp.onError(Status.INVALID_ARGUMENT
        .withDescription(p.second)
        .asException());
      return;
    }

    var setup = p.first;
    var data = MatrixData.of(db, setup);
    var provider = ResultProviders.lazyOf(db, data);
    var result = new FullResult(provider);

    var key = UUID.randomUUID().toString();
    results.put(key, result);
    var r = Result.newBuilder()
      .setId(key)
      .build();
    resp.onNext(r);
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
  public void getTechFlows(Result req, StreamObserver<ResultsProto.TechFlow> resp) {
    var result = results.get(req.getId());
    if (result == null) {
      Response.notFound(resp, "Result does not exist: " + req.getId());
      return;
    }
    var refData = Refs.dataOf(db);
    for (var product : result.techIndex()) {
      resp.onNext(Results.toProto(product, refData));
    }
    resp.onCompleted();
  }

  @Override
  public void getEnviFlows(Result req, StreamObserver<ResultsProto.EnviFlow> resp) {
    var result = results.get(req.getId());
    if (result == null) {
      Response.notFound(resp, "Result does not exist: " + req.getId());
      return;
    }
    var flows = result.flowIndex();
    if (flows == null) {
      resp.onCompleted();
      return;
    }
    var refData = Refs.dataOf(db);
    for (var flow : flows) {
      resp.onNext(Results.toProto(flow, refData));
    }
    resp.onCompleted();
  }

  @Override
  public void getImpactCategories(Result req, StreamObserver<Proto.Ref> resp) {
    var result = results.get(req.getId());
    if (result == null) {
      Response.notFound(resp, "Result does not exist: " + req.getId());
      return;
    }
    var impacts = result.impactIndex();
    if (impacts == null)
      return;
    var refData = Refs.dataOf(db);
    for (var impact : impacts) {
      resp.onNext(Refs.refOf(impact, refData).build());
    }
    resp.onCompleted();
  }

  @Override
  public void getTotalInventory(
    Result req, StreamObserver<ResultsProto.ResultValue> resp) {

    // TODO throw an error if the result does not exist,
    // TODO maybe wrap with `withResult`
    // get the flow results
    var result = results.get(req.getId());
    if (result == null) {
      resp.onCompleted();
      return;
    }
    var flows = result.flowIndex();
    if (flows == null) {
      resp.onCompleted();
      return;
    }

    var refData = Refs.dataOf(db);
    for (var flow : flows) {
      var value = result.getTotalFlowResult(flow);
      if (value == 0)
        return;
      resp.onNext(Results.toProtoResult(flow, refData, value));
    }
    resp.onCompleted();
  }

  @Override
  public void getTotalImpacts(
    Result req, StreamObserver<ResultsProto.ResultValue> resp) {

    // get the impact results
    var result = results.get(req.getId());
    if (result == null) {
      resp.onCompleted();
      return;
    }
    var impacts = result.impactIndex();
    if (impacts == null) {
      resp.onCompleted();
      return;
    }

    var refData = Refs.dataOf(db);
    for (var impact : impacts) {
      var value = result.getTotalImpactResult(impact);
      var proto = ResultsProto.ResultValue.newBuilder()
        .setImpact(Refs.refOf(impact, refData))
        .setValue(value)
        .build();
      resp.onNext(proto);
    }
    resp.onCompleted();
  }

  @Override
  public void getImpactFactors(
    ImpactFactorRequest req, StreamObserver<ImpactFactorResponse> resp) {

    // check that we have a result with  flows and impacts
    var result = results.get(req.getResult().getId());
    if (result == null) {
      resp.onError(Status.INVALID_ARGUMENT
        .withDescription("Invalid result ID")
        .asException());
      return;
    }
    var flowIndex = result.flowIndex();
    var impactIndex = result.impactIndex();
    if (flowIndex == null || impactIndex == null) {
      resp.onCompleted();
      return;
    }

    // check that we have at least an indicator or flow
    var indicator = Results.findImpact(result, req.getIndicator());
    var flow = Results.findFlow(result, req.getFlow());
    if (flow == null && indicator == null) {
      resp.onCompleted();
      return;
    }

    var refData = Refs.dataOf(db);

    // get one specific factor of an indicator and flow
    if (indicator != null && flow != null) {
      var factor = ImpactFactorResponse.newBuilder()
        .setIndicator(Refs.refOf(indicator))
        .setFlow(Results.toProto(flow, refData))
        .setValue(result.getImpactFactor(indicator, flow));
      resp.onNext(factor.build());
      resp.onCompleted();
      return;
    }

    // get non-zero factors of an indicator
    if (flow == null) {
      var indicatorRef = Refs.refOf(indicator);
      for (var iFlow : flowIndex) {
        var value = result.getImpactFactor(indicator, iFlow);
        if (value == 0)
          continue;
        var factor = ImpactFactorResponse.newBuilder()
          .setIndicator(indicatorRef)
          .setFlow(Results.toProto(iFlow, refData))
          .setValue(value);
        resp.onNext(factor.build());
      }
      resp.onCompleted();
      return;
    }

    // get all impact factors of a flow
    for (var impact : impactIndex) {
      var factor = ImpactFactorResponse.newBuilder()
        .setIndicator(Refs.refOf(impact))
        .setFlow(Results.toProto(flow, refData))
        .setValue(result.getImpactFactor(impact, flow));
      resp.onNext(factor.build());
    }
    resp.onCompleted();
  }

  @Override
  public void getDirectContribution(
    TechFlowContributionRequest req,
    StreamObserver<ResultsProto.ResultValue> resp) {

    TechFlowContribution.of(this, req, resp)
      .ifImpact(FullResult::getDirectImpactResult)
      .ifFlow(FullResult::getDirectFlowResult)
      .ifCosts(FullResult::getDirectCostResult)
      .close();
  }

  @Override
  public void getTotalContribution(
    TechFlowContributionRequest req,
    StreamObserver<ResultsProto.ResultValue> resp) {

    TechFlowContribution.of(this, req, resp)
      .ifImpact(FullResult::getUpstreamImpactResult)
      .ifFlow(FullResult::getUpstreamFlowResult)
      .ifCosts(FullResult::getUpstreamCostResult)
      .close();
  }

  @Override
  public void getTotalContributionOfOne(
    TechFlowContributionRequest req,
    StreamObserver<ResultsProto.ResultValue> resp) {

    TechFlowContribution.of(this, req, resp)
      .ifImpact((result, product, impact) -> {
        var productIdx = result.techIndex().of(product);
        var impactIdx = result.impactIndex().of(impact);
        return result.provider.totalImpactOfOne(impactIdx, productIdx);
      })
      .ifFlow((result, product, flow) -> {
        var productIdx = result.techIndex().of(product);
        var flowIdx = result.flowIndex().of(flow);
        var value = result.provider.totalFlowOfOne(flowIdx, productIdx);
        return result.adopt(flow, value);
      })
      .ifCosts((result, product) -> {
        var productIdx = result.techIndex().of(product);
        return result.provider.totalCostsOfOne(productIdx);
      })
      .close();
  }

  @Override
  public void dispose(Result req, StreamObserver<Empty> resp) {
    results.remove(req.getId());
    // we always return ok, even when the result does not exist
    resp.onNext(Empty.newBuilder().build());
    resp.onCompleted();
  }

}
