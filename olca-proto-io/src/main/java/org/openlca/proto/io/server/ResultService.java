package org.openlca.proto.io.server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.DbEntityResolver;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.results.FullResult;
import org.openlca.proto.ProtoCalculationSetup;
import org.openlca.proto.ProtoRef;
import org.openlca.proto.grpc.ImpactFactorRequest;
import org.openlca.proto.grpc.ImpactFactorResponse;
import org.openlca.proto.grpc.ProtoEnviFlow;
import org.openlca.proto.grpc.ProtoResultRef;
import org.openlca.proto.grpc.ProtoTechFlow;
import org.openlca.proto.grpc.ResultServiceGrpc;
import org.openlca.proto.grpc.ResultValue;
import org.openlca.proto.grpc.TechFlowContributionRequest;
import org.openlca.proto.io.input.CalculationSetupReader;
import org.openlca.proto.io.output.Refs;

import com.google.protobuf.Empty;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

class ResultService extends ResultServiceGrpc.ResultServiceImplBase {

  final IDatabase db;
  final Map<String, FullResult> results = new HashMap<>();

  ResultService(IDatabase db) {
    this.db = db;
  }

  @Override
  public void calculate(
    ProtoCalculationSetup req, StreamObserver<ProtoResultRef> resp) {

    var setup = CalculationSetupReader.read(DbEntityResolver.of(db), req);
    if (setup == null) {
      resp.onError(Status.INVALID_ARGUMENT
        .withDescription("invalid calculation setup")
        .asException());
      return;
    }

    var result = new SystemCalculator(db)
    		.calculateFull(setup);
    var key = UUID.randomUUID().toString();
    results.put(key, result);
    var r = ProtoResultRef.newBuilder()
      .setId(key)
      .build();
    resp.onNext(r);
    resp.onCompleted();
  }

  @Override
  public void getTechFlows(
    ProtoResultRef req, StreamObserver<ProtoTechFlow> resp) {
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
  public void getEnviFlows(
    ProtoResultRef req, StreamObserver<ProtoEnviFlow> resp) {
    var result = results.get(req.getId());
    if (result == null) {
      Response.notFound(resp, "Result does not exist: " + req.getId());
      return;
    }
    var flows = result.enviIndex();
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
  public void getImpactCategories(
    ProtoResultRef req, StreamObserver<ProtoRef> resp) {
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
    ProtoResultRef req, StreamObserver<ResultValue> resp) {

    // TODO maybe wrap with `withResult`
    var result = results.get(req.getId());
    if (result == null) {
      Response.notFound(resp, "Result does not exist: " + req.getId());
      return;
    }
    var flows = result.enviIndex();
    if (flows == null) {
      resp.onCompleted();
      return;
    }

    var refData = Refs.dataOf(db);
    for (var flow : flows) {
      var value = result.getTotalFlowResult(flow);
      if (value == 0)
        continue;
      resp.onNext(Results.toProtoResult(flow, refData, value));
    }
    resp.onCompleted();
  }

  @Override
  public void getTotalImpacts(
    ProtoResultRef req, StreamObserver<ResultValue> resp) {

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
      var proto = ResultValue.newBuilder()
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
    var flowIndex = result.enviIndex();
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
    TechFlowContributionRequest req, StreamObserver<ResultValue> resp) {

    TechFlowContribution.of(this, req, resp)
      .ifImpact(FullResult::getDirectImpactResult)
      .ifFlow(FullResult::getDirectFlowResult)
      .ifCosts(FullResult::getDirectCostResult)
      .close();
  }

  @Override
  public void getTotalContribution(
    TechFlowContributionRequest req, StreamObserver<ResultValue> resp) {

    TechFlowContribution.of(this, req, resp)
      .ifImpact(FullResult::getUpstreamImpactResult)
      .ifFlow(FullResult::getUpstreamFlowResult)
      .ifCosts(FullResult::getUpstreamCostResult)
      .close();
  }

  @Override
  public void getTotalContributionOfOne(
    TechFlowContributionRequest req,
    StreamObserver<ResultValue> resp) {

    TechFlowContribution.of(this, req, resp)
      .ifImpact((result, product, impact) -> {
        var productIdx = result.techIndex().of(product);
        var impactIdx = result.impactIndex().of(impact);
        return result.provider().totalImpactOfOne(impactIdx, productIdx);
      })
      .ifFlow((result, product, flow) -> {
        var productIdx = result.techIndex().of(product);
        var flowIdx = result.enviIndex().of(flow);
        var value = result.provider().totalFlowOfOne(flowIdx, productIdx);
        return result.adopt(flow, value);
      })
      .ifCosts((result, product) -> {
        var productIdx = result.techIndex().of(product);
        return result.provider().totalCostsOfOne(productIdx);
      })
      .close();
  }

  @Override
  public void dispose(ProtoResultRef req, StreamObserver<Empty> resp) {
    results.remove(req.getId());
    // we always return ok, even when the result does not exist
    resp.onNext(Empty.newBuilder().build());
    resp.onCompleted();
  }

}
