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

public class CostHandler {

	private final HandlerContext context;
	private final Utils utils;
	private final IDatabase db;

	public CostHandler(HandlerContext context) {
		this.context = context;
		this.utils = new Utils(context);
		this.db = context.db();
	}

	@Rpc("get/costs/direct_contributions")
	public RpcResponse getTotalRequirements(RpcRequest req) {
		var cached = context.getCachedResultOf(req);
		if (cached.isError())
			return cached.error();
		var cachedResult= cached.value();
		var r = cachedResult.requireResult(req);
		if (r.isError())
			return r.error();
		var result = r.value();
		var array = new JsonArray();
		for (var techFlow : result.techIndex()) {
			var tr = result.getTotalRequirementsOf(techFlow);
			var obj = new JsonObject();
			obj.add("provider",
				JsonRpc.encodeTechFlow(techFlow, cachedResult.refs()));
			obj.addProperty("amount", tr);
			obj.addProperty("costs", result.getDirectCostResult(techFlow));
			array.add(obj);
		}
		return Responses.ok(array, req);
	}

	@Rpc("get/costs/upstream/added_value")
	public RpcResponse getUpstreamAddedValue(RpcRequest req) {
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
