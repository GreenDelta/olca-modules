package org.openlca.proto.server;

import java.util.function.ToDoubleBiFunction;

import io.grpc.stub.StreamObserver;
import org.openlca.core.matrix.index.IndexFlow;
import org.openlca.core.matrix.index.ProcessProduct;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.FullResult;
import org.openlca.proto.generated.results.ResultsProto;
import org.openlca.proto.generated.results.TechFlowContributionRequest;
import org.openlca.proto.output.Refs;

class TechFlowContribution {

  private final StreamObserver<ResultsProto.ResultValue> resp;
  private final ResultService service;

  private FullResult result;
  private ProcessProduct product;
  private ImpactDescriptor impact;
  private IndexFlow flow;
  private boolean forCosts;
  private boolean isClosed;

  static TechFlowContribution of(
    ResultService service,
    TechFlowContributionRequest req,
    StreamObserver<ResultsProto.ResultValue> resp) {

    var resolved = new TechFlowContribution(service, resp);

    // a valid result is required
    if (!req.hasResult() || !req.hasTechFlow())
      return resolved.error(
        "A valid result and tech-flow are required");
    var resultId = req.getResult().getId();
    var result = service.results.get(resultId);
    if (result == null)
      return resolved.error("The result does not exist: " + resultId);
    resolved.result = result;

    // also, the product is required
    resolved.product = Results.findProduct(result, req.getTechFlow());
    if (resolved.product == null) {
      return resolved.error("The product does not exist in the result");
    }

    // set the result selector
    if (req.hasEnviFlow() && result.hasFlowResults()) {
      resolved.flow = Results.findFlow(result, req.getEnviFlow());
    } else if (req.hasImpact() && result.hasImpactResults()) {
      resolved.impact = Results.findImpact(result, req.getImpact());
    } else if (req.getCosts() && result.hasCostResults()) {
      resolved.forCosts = true;
    }

    return resolved;
  }

  private TechFlowContribution(
    ResultService service,
    StreamObserver<ResultsProto.ResultValue> resp) {
    this.service = service;
    this.resp = resp;
  }

  TechFlowContribution ifImpact(
    ToDoubleTriFunction<FullResult, ProcessProduct, ImpactDescriptor> fn) {
    if (isClosed || impact == null)
      return this;
    closeWith(fn.applyAsDouble(result, product, impact));
    return this;
  }

  TechFlowContribution ifFlow(
    ToDoubleTriFunction<FullResult, ProcessProduct, IndexFlow> fn) {
    if (isClosed || flow == null)
      return this;
    closeWith(fn.applyAsDouble(result, product, flow));
    return this;
  }

  TechFlowContribution ifCosts(
    ToDoubleBiFunction<FullResult, ProcessProduct> fn) {
    if (isClosed || !forCosts)
      return this;
    closeWith(fn.applyAsDouble(result, product));
    return this;
  }

  private void closeWith(double value) {
    if (isClosed)
      return;
    var refData = Refs.dataOf(service.db);
    var proto = ResultsProto.ResultValue.newBuilder()
      .setTechFlow(Results.toProto(product, refData))
      .setValue(value)
      .build();
    resp.onNext(proto);
    resp.onCompleted();
    isClosed = true;
  }

  private TechFlowContribution error(String message) {
    Response.invalidArg(resp, message);
    isClosed = true;
    return this;
  }

  void close() {
    if (isClosed)
      return;
    closeWith(0);
  }

  @FunctionalInterface
  interface ToDoubleTriFunction<T, U, V> {
    double applyAsDouble(T t, U u, V v);
  }
}
