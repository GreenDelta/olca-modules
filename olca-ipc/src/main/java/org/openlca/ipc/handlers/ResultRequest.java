package org.openlca.ipc.handlers;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.results.FullResult;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.output.DbRefs;

import java.util.function.Function;

record ResultRequest(
	FullResult result,
	TechFlow techFlow,
	EnviFlow enviFlow,
	ImpactDescriptor impact,
	LocationDescriptor location,
	DbRefs refs) {

	RpcResponse of(RpcRequest req, HandlerContext context,
	               Function<ResultRequest, RpcResponse> handler) {

	}



	RpcResponse  impactMissing() {
	}



}
