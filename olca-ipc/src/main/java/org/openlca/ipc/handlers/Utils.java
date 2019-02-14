package org.openlca.ipc.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.Daos;
import org.openlca.core.database.EntityCache;
import org.openlca.core.math.CalculationType;
import org.openlca.core.matrix.DIndex;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.ContributionItem;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.ipc.Responses;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.ipc.handlers.Upstream.StringPair;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class Utils {

	private final HandlerContext ctx;

	Utils(HandlerContext context) {
		this.ctx = context;
	}

	<T> T getResult(JsonObject json) {
		String resultID = Json.getString(json, "resultId");
		if (resultID == null)
			throw new IllegalArgumentException("No result ID");
		T result = getResult(resultID);
		if (result == null)
			throw new IllegalArgumentException("No result found for given ID");
		return result;
	}

	@SuppressWarnings("unchecked")
	private <T> T getResult(String id) {
		CachedResult<?> result = (CachedResult<?>) ctx.cache.get(id);
		if (result.setup.type == CalculationType.SIMPLE_CALCULATION
				|| result.setup.type == CalculationType.MONTE_CARLO_SIMULATION)
			return null;
		return (T) result.result;
	}

	String getUnit(FlowDescriptor flow, EntityCache cache) {
		FlowProperty prop = cache.get(FlowProperty.class, flow.refFlowPropertyId);
		if (prop == null || prop.unitGroup == null)
			return null;
		Unit unit = prop.unitGroup.referenceUnit;
		if (unit == null)
			return null;
		return unit.name;
	}

	@SuppressWarnings("unchecked")
	<T1 extends RootEntity, T2 extends BaseDescriptor> List<ContributionItem<T2>> toDescriptorContributions(
			List<ContributionItem<T1>> items) {
		List<ContributionItem<T2>> contributions = new ArrayList<>();
		items.forEach(i -> {
			ContributionItem<T2> item = new ContributionItem<>();
			item.item = (T2) Descriptors.toDescriptor(i.item);
			item.amount = i.amount;
			item.rest = i.rest;
			item.share = i.share;
			contributions.add(item);
		});
		return contributions;
	}

	RpcResponse handle1(RpcRequest req, ResultHandler1 handler) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No parameter given", req);
		JsonObject json = req.params.getAsJsonObject();
		SimpleResult result = getResult(json);
		EntityCache cache = EntityCache.create(ctx.db);
		return Responses.ok(handler.handle(result, cache), req);
	}

	RpcResponse handle2(RpcRequest req, ResultHandler2 handler) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No parameter given", req);
		JsonObject json = req.params.getAsJsonObject();
		ContributionResult result = getResult(json);
		FlowDescriptor flow = get(result.flowIndex, json, "flow");
		if (flow == null)
			return Responses.invalidParams("Missing or invalid flow parameter", req);
		EntityCache cache = EntityCache.create(ctx.db);
		return Responses.ok(handler.handle(result, flow, cache), req);
	}

	RpcResponse handle3(RpcRequest req, ResultHandler3 handler) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No parameter given", req);
		JsonObject json = req.params.getAsJsonObject();
		ContributionResult result = getResult(json);
		FlowDescriptor flow = get(result.flowIndex, json, "flow");
		if (flow == null)
			return Responses.invalidParams("Missing or invalid flow parameter", req);
		LocationDescriptor location = get(ModelType.LOCATION, json);
		if (location == null)
			return Responses.invalidParams("Missing or invalid location parameter", req);
		EntityCache cache = EntityCache.create(ctx.db);
		return Responses.ok(handler.handle(result, flow, location, cache), req);
	}

	RpcResponse handle4(RpcRequest req, ResultHandler4 handler) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No parameter given", req);
		JsonObject json = req.params.getAsJsonObject();
		ContributionResult result = getResult(json);
		ImpactCategoryDescriptor impact = get(result.impactIndex, json, "impactCategory");
		if (impact == null)
			return Responses.invalidParams("Missing or invalid impact category parameter", req);
		EntityCache cache = EntityCache.create(ctx.db);
		return Responses.ok(handler.handle(result, impact, cache), req);
	}

	RpcResponse handle5(RpcRequest req, ResultHandler5 handler) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No parameter given", req);
		JsonObject json = req.params.getAsJsonObject();
		ContributionResult result = getResult(json);
		ImpactCategoryDescriptor impact = get(result.impactIndex, json, "impactCategory");
		if (impact == null)
			return Responses.invalidParams("Missing or invalid impact category parameter", req);
		ProcessDescriptor process = get(ModelType.PROCESS, json, result.techIndex.getProcessIds());
		if (process == null)
			return Responses.invalidParams("Missing or invalid process parameter", req);
		EntityCache cache = EntityCache.create(ctx.db);
		return Responses.ok(handler.handle(result, impact, process, cache), req);
	}

	RpcResponse handle6(RpcRequest req, ResultHandler6 handler) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No parameter given", req);
		JsonObject json = req.params.getAsJsonObject();
		ContributionResult result = getResult(json);
		ImpactCategoryDescriptor impact = get(result.impactIndex, json, "impactCategory");
		if (impact == null)
			return Responses.invalidParams("Missing or invalid impact category parameter", req);
		LocationDescriptor location = get(ModelType.LOCATION, json);
		if (location == null)
			return Responses.invalidParams("Missing or invalid location parameter", req);
		EntityCache cache = EntityCache.create(ctx.db);
		return Responses.ok(handler.handle(result, impact, location, cache), req);
	}

	RpcResponse handle7(RpcRequest req, ResultHandler7 handler) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No parameter given", req);
		JsonObject json = req.params.getAsJsonObject();
		ContributionResult result = getResult(json);
		ImpactCategoryDescriptor impact = get(result.impactIndex, json, "impactCategory");
		if (impact == null)
			return Responses.invalidParams("Missing or invalid impact category parameter", req);
		LocationDescriptor location = get(ModelType.LOCATION, json);
		if (location == null)
			return Responses.invalidParams("Missing or invalid location parameter", req);
		ProcessDescriptor process = get(ModelType.PROCESS, json, result.techIndex.getProcessIds());
		if (process == null)
			return Responses.invalidParams("Missing or invalid process parameter", req);
		EntityCache cache = EntityCache.create(ctx.db);
		return Responses.ok(handler.handle(result, impact, location, process, cache), req);
	}

	RpcResponse handle8(RpcRequest req, ResultHandler8 handler) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No parameter given", req);
		JsonObject json = req.params.getAsJsonObject();
		FullResult result = getResult(json);
		ProcessDescriptor process = get(ModelType.PROCESS, json, result.techIndex.getProcessIds());
		if (process == null)
			return Responses.invalidParams("Missing or invalid process parameter", req);
		EntityCache cache = EntityCache.create(ctx.db);
		return Responses.ok(handler.handle(result, process, cache), req);
	}

	RpcResponse handle9(RpcRequest req, ResultHandler9 handler) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No parameter given", req);
		JsonObject json = req.params.getAsJsonObject();
		FullResult result = getResult(json);
		FlowDescriptor flow = get(result.flowIndex, json, "flow");
		if (flow == null)
			return Responses.invalidParams("Missing or invalid flow parameter", req);
		EntityCache cache = EntityCache.create(ctx.db);
		return Responses.ok(handler.handle(result, flow, cache), req);
	}

	RpcResponse handle10(RpcRequest req, ResultHandler10 handler) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No parameter given", req);
		JsonObject json = req.params.getAsJsonObject();
		FullResult result = getResult(json);
		ImpactCategoryDescriptor impact = get(result.impactIndex, json, "impactCategory");
		if (impact == null)
			return Responses.invalidParams("Missing or invalid impact category parameter", req);
		EntityCache cache = EntityCache.create(ctx.db);
		return Responses.ok(handler.handle(result, impact, cache), req);
	}

	private <T extends BaseDescriptor> T get(DIndex<T> index, JsonObject json, String field) {
		String flowID = Json.getRefId(json, field);
		if (flowID == null)
			return null;
		for (T descriptor : index.content())
			if (flowID.equals(descriptor.refId))
				return descriptor;
		return null;
	}

	private <T extends CategorizedDescriptor> T get(ModelType type, JsonObject json) {
		return get(type, json, null);
	}

	@SuppressWarnings("unchecked")
	private <T extends CategorizedDescriptor> T get(ModelType type, JsonObject json, Set<Long> allowed) {
		String flowID = Json.getRefId(json, type.name().toLowerCase());
		if (flowID == null)
			return null;
		T descriptor = (T) Daos.categorized(ctx.db, type).getDescriptorForRefId(flowID);
		if (allowed != null && !allowed.contains(descriptor.id))
			return null;
		return descriptor;
	}
	
	List<StringPair> parseProducts(RpcRequest req) {
		JsonObject json = req.params.getAsJsonObject();
		if (!json.has("path") || !json.get("path").isJsonArray())
			return new ArrayList<>();
		JsonArray path = json.get("path").getAsJsonArray();
		List<StringPair> products = new ArrayList<>();
		for (JsonElement element : path) {
			if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString())
				continue;
			String entry = element.getAsString();
			String[] ids = entry.split("/");
			if (ids.length != 2) 
				continue;
			products.add(new StringPair(ids[0], ids[1]));
		}
		return products;
	}

	interface ResultHandler1 {

		JsonElement handle(SimpleResult result, EntityCache cache);

	}

	interface ResultHandler2 {

		JsonElement handle(ContributionResult result, FlowDescriptor flow, EntityCache cache);

	}

	interface ResultHandler3 {

		JsonElement handle(ContributionResult result, FlowDescriptor flow, LocationDescriptor location,
				EntityCache cache);

	}

	interface ResultHandler4 {

		JsonElement handle(ContributionResult result, ImpactCategoryDescriptor impact, EntityCache cache);

	}

	interface ResultHandler5 {

		JsonElement handle(ContributionResult result, ImpactCategoryDescriptor impact, ProcessDescriptor process,
				EntityCache cache);

	}

	interface ResultHandler6 {

		JsonElement handle(ContributionResult result, ImpactCategoryDescriptor impact, LocationDescriptor location,
				EntityCache cache);

	}

	interface ResultHandler7 {

		JsonElement handle(ContributionResult result, ImpactCategoryDescriptor impact, LocationDescriptor location,
				ProcessDescriptor process, EntityCache cache);

	}

	interface ResultHandler8 {

		JsonElement handle(FullResult result, ProcessDescriptor process, EntityCache cache);

	}

	interface ResultHandler9 {

		JsonElement handle(FullResult result, FlowDescriptor flow, EntityCache cache);

	}

	interface ResultHandler10 {

		JsonElement handle(FullResult result, ImpactCategoryDescriptor impact, EntityCache cache);

	}

}
