package org.openlca.ipc.handlers;

import java.util.List;

import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Currency;
import org.openlca.core.results.UpstreamNode;
import org.openlca.core.results.UpstreamTree;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.ipc.handlers.Upstream.StringPair;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

public class CostHandler {

	private final HandlerContext context;
	private final Utils utils;
	private final IDatabase db;

	public CostHandler(HandlerContext context) {
		this.context = context;
		this.utils = new Utils(context);
		this.db = context.db();
	}

	@Rpc("get/costs/total_requirements")
	public RpcResponse getTotalRequirements(RpcRequest req) {
		return ResultRequest.of(req, context, rr -> {
			var result = rr.result();
			var array = JsonRpc.arrayOf(result.techIndex(), techFlow -> {
				var obj = new JsonObject();
				Json.put(obj, "provider", JsonRpc.encodeTechFlow(techFlow, rr.refs()));
				Json.put(obj, "amount", result.getTotalRequirementsOf(techFlow));
				Json.put(obj, "costs", result.getDirectCostResult(techFlow));
				return obj;
			});
			return Responses.ok(array, req);
		});
	}

	@Rpc("get/costs/upstream/added_value")
	public RpcResponse getUpstreamAddedValue(RpcRequest req) {
		return ResultRequest.of(req, context, rr -> {

		});
		return utils.full(req, (result, cache) -> getUpstream(req, result.getAddedValueTree(), cache));
	}

	@Rpc("get/costs/upstream/net_costs")
	public RpcResponse getUpstreamNetCosts(RpcRequest req) {
		return utils.full(req, (result, cache) -> getUpstream(req, result.getCostTree(), cache));
	}

	private JsonArray getUpstream(RpcRequest req, UpstreamTree tree, EntityCache cache) {
		List<StringPair> products = utils.parseProducts(req);
		List<UpstreamNode> results = Upstream.calculate(tree, products);
		String code = getReferenceCurrencyCode();
		return JsonRpc.encode(results, tree, cache, json -> {
			json.addProperty("unit", code);
			json.add("upstream", json.remove("amount"));
		});
	}

	private String getReferenceCurrencyCode() {
		CurrencyDao dao = new CurrencyDao(db);
		Currency c = dao.getReferenceCurrency();
		if (c != null && c.code != null)
			return c.code;
		return "?";
	}

}
