package org.openlca.proto.io.output;

import org.openlca.core.model.FlowResult;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.Result;
import org.openlca.jsonld.Json;
import org.openlca.proto.ProtoFlowResult;
import org.openlca.proto.ProtoImpactResult;
import org.openlca.proto.ProtoResult;
import org.openlca.proto.ProtoType;
import org.openlca.util.Strings;

import java.util.Objects;

public class ResultWriter {

	private final WriterConfig config;

	public ResultWriter(WriterConfig config) {
		this.config = config;
	}

	public ProtoResult write(Result result) {
		var proto = ProtoResult.newBuilder();
		if (result == null)
			return proto.build();
		proto.setType(ProtoType.Result);
		Out.map(result, proto);

		config.dep(result.impactMethod, proto::setImpactMethod);
		config.dep(result.productSystem, proto::setProductSystem);
		result.impactResults.forEach(
			impactResult -> proto.addImpactResults(mapImpactResultOf(impactResult)));
		var refFlow = result.referenceFlow;
		result.flowResults.forEach(
			flowResult ->
				proto.addFlowResults(mapFlowResultOf(flowResult, refFlow)));

		return proto.build();
	}

	private ProtoFlowResult.Builder mapFlowResultOf(FlowResult result,
		FlowResult refFlow) {
		var proto = ProtoFlowResult.newBuilder();

		// object references
		config.dep(result.flow, proto::setFlow);
		config.dep(Json.propertyOf(result), proto::setFlowProperty);
		config.dep(Json.unitOf(result), proto::setUnit);
		config.dep(result.location, proto::setLocation);

		// other attributes
		proto.setIsInput(result.isInput);
		proto.setIsRefFlow((Objects.equals(result, refFlow)));
		proto.setAmount(result.amount);
		proto.setDescription(Strings.orEmpty(result.description));

		return proto;
	}

	private ProtoImpactResult.Builder mapImpactResultOf(ImpactResult results) {
		var proto = ProtoImpactResult.newBuilder();
		config.dep(results.indicator, proto::setIndicator);
		proto.setAmount(results.amount);
		proto.setDescription(Strings.orEmpty(results.description));
		return proto;
	}

}
