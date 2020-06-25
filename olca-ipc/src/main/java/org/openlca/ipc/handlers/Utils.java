package org.openlca.ipc.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.openlca.core.database.Daos;
import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.DIndex;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.BaseResult;
import org.openlca.core.results.Contribution;
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

	<T extends BaseResult> T getResult(JsonObject json) {
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
		if (result == null)
			return null;
		return (T) result.result;
	}

	String getUnit(IndexFlow flow, EntityCache cache) {
		if (flow == null || flow.flow == null)
			return null;
		FlowProperty prop = cache.get(
				FlowProperty.class, flow.flow.refFlowPropertyId);
		if (prop == null || prop.unitGroup == null)
			return null;
		Unit unit = prop.unitGroup.referenceUnit;
		if (unit == null)
			return null;
		return unit.name;
	}

	@SuppressWarnings("unchecked")
	<T1 extends RootEntity, T2 extends Descriptor> List<Contribution<T2>> toDescriptors(
			List<Contribution<T1>> items) {
		List<Contribution<T2>> contributions = new ArrayList<>();
		items.forEach(i -> {
			Contribution<T2> item = new Contribution<>();
			item.item = (T2) Descriptor.of(i.item);
			item.amount = i.amount;
			item.isRest = i.isRest;
			item.share = i.share;
			contributions.add(item);
		});
		return contributions;
	}

	RpcResponse simple(RpcRequest req, Simple handler) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No parameter given", req);
		JsonObject json = req.params.getAsJsonObject();
		SimpleResult result = getResult(json);
		EntityCache cache = EntityCache.create(ctx.db);
		return Responses.ok(handler.handle(result, cache), req);
	}

	RpcResponse contribution(RpcRequest req, ContributionHandler handler) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No parameter given", req);
		JsonObject json = req.params.getAsJsonObject();
		ContributionResult result = getResult(json);
		EntityCache cache = EntityCache.create(ctx.db);
		return Responses.ok(handler.handle(result, cache), req);
	}

	RpcResponse contributionFlow(RpcRequest req, ContributionFlow handler) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No parameter given", req);
		JsonObject json = req.params.getAsJsonObject();
		ContributionResult result = getResult(json);
		IndexFlow flow = get(result.flowIndex, json, "flow");
		if (flow == null)
			return Responses.invalidParams("Missing or invalid flow parameter", req);
		EntityCache cache = EntityCache.create(ctx.db);
		return Responses.ok(handler.handle(result, flow, cache), req);
	}

	RpcResponse contributionFlowLocation(RpcRequest req, ContributionFlowLocation handler) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No parameter given", req);
		JsonObject json = req.params.getAsJsonObject();
		ContributionResult result = getResult(json);
		IndexFlow flow = get(result.flowIndex, json, "flow");
		if (flow == null || flow.flow == null)
			return Responses.invalidParams("Missing or invalid flow parameter", req);
		LocationDescriptor location = get(ModelType.LOCATION, json);
		if (location == null)
			return Responses.invalidParams("Missing or invalid location parameter", req);
		EntityCache cache = EntityCache.create(ctx.db);
		return Responses.ok(handler.handle(result, flow, location, cache), req);
	}

	RpcResponse contributionImpact(RpcRequest req, ContributionImpact handler) {
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

	RpcResponse contributionImpactProcess(RpcRequest req, ContributionImpactProcess handler) {
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

	RpcResponse contributionImpactLocation(RpcRequest req, ContributionImpactLocation handler) {
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

	RpcResponse contributionImpactLocationProcess(RpcRequest req, ContributionImpactLocationProcess handler) {
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

	RpcResponse full(RpcRequest req, Full handler) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No parameter given", req);
		JsonObject json = req.params.getAsJsonObject();
		FullResult result = getResult(json);
		EntityCache cache = EntityCache.create(ctx.db);
		return Responses.ok(handler.handle(result, cache), req);
	}

	RpcResponse fullFlow(RpcRequest req, FullFlow handler) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No parameter given", req);
		JsonObject json = req.params.getAsJsonObject();
		FullResult result = getResult(json);
		IndexFlow flow = get(result.flowIndex, json, "flow");
		if (flow == null || flow.flow == null)
			return Responses.invalidParams("Missing or invalid flow parameter", req);
		EntityCache cache = EntityCache.create(ctx.db);
		return Responses.ok(handler.handle(result, flow, cache), req);
	}

	RpcResponse fullProcess(RpcRequest req, FullProcess handler) {
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

	RpcResponse fullImpact(RpcRequest req, FullImpact handler) {
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

	private <T extends Descriptor> T get(
			DIndex<T> index, JsonObject json, String field) {
		if (index == null)
			return null;
		String refID = Json.getRefId(json, field);
		if (refID == null)
			return null;
		for (T d : index.content()) {
			if (refID.equals(d.refId))
				return d;
		}
		return null;
	}

	private IndexFlow get(FlowIndex idx, JsonObject json,
			String field) {
		if (idx == null)
			return null;
		String refID = Json.getRefId(json, field);
		if (refID == null)
			return null;
		for (IndexFlow f : idx.flows()) {
			if (f.flow == null)
				continue;
			if (refID.equals(f.flow.refId))
				return f;
		}
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

	<T> List<T> filter(List<T> list, Function<T, Boolean> predicate) {
		List<T> filtered = new ArrayList<>();
		for (T element : list) {
			if (predicate.apply(element)) {
				filtered.add(element);
			}
		}
		return filtered;
	}
	
	interface Simple {

		JsonElement handle(SimpleResult result, EntityCache cache);

	}

	interface ContributionHandler {

		JsonElement handle(ContributionResult result, EntityCache cache);

	}

	interface ContributionFlow {

		JsonElement handle(ContributionResult result, IndexFlow flow, EntityCache cache);

	}

	interface ContributionFlowLocation {

		JsonElement handle(ContributionResult result, IndexFlow flow, LocationDescriptor location,
				EntityCache cache);

	}

	interface ContributionImpact {

		JsonElement handle(ContributionResult result, ImpactCategoryDescriptor impact, EntityCache cache);

	}

	interface ContributionImpactProcess {

		JsonElement handle(ContributionResult result, ImpactCategoryDescriptor impact, ProcessDescriptor process,
				EntityCache cache);

	}

	interface ContributionImpactLocation {

		JsonElement handle(ContributionResult result, ImpactCategoryDescriptor impact, LocationDescriptor location,
				EntityCache cache);

	}

	interface ContributionImpactLocationProcess {

		JsonElement handle(ContributionResult result, ImpactCategoryDescriptor impact, LocationDescriptor location,
				ProcessDescriptor process, EntityCache cache);

	}
	
	interface Full {

		JsonElement handle(FullResult result, EntityCache cache);

	}

	interface FullFlow {

		JsonElement handle(FullResult result, IndexFlow flow, EntityCache cache);

	}

	interface FullProcess {

		JsonElement handle(FullResult result, ProcessDescriptor process, EntityCache cache);

	}

	interface FullImpact {

		JsonElement handle(FullResult result, ImpactCategoryDescriptor impact, EntityCache cache);

	}

}
