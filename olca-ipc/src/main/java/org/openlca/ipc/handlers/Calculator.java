package org.openlca.ipc.handlers;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.NwSetDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.CalculationType;
import org.openlca.core.math.Simulator;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.SimpleResult;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;
import org.openlca.util.Pair;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class Calculator {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final HandlerContext context;
	private final IDatabase db;

	public Calculator(HandlerContext context) {
		this.context = context;
		this.db = context.db;
	}

	/**
	 * Creates a Monte-Carlo simulator for a calculation setup.
	 */
	@Rpc("simulator")
	public RpcResponse simulator(RpcRequest req) {
		var p = setupOf(req);
		if (p.second != null)
			return p.second;
		var setup = p.first;
		log.info("Create simulator for system {}", setup.productSystem.refId);
		Simulator simulator = Simulator.create(setup, db, context.solver);
		String id = UUID.randomUUID().toString();
		JsonObject obj = new JsonObject();
		obj.addProperty("@id", id);
		obj.addProperty("@type", "Simulator");
		context.cache.put(id, CachedResult.of(setup, simulator));
		return Responses.ok(obj, req);
	}

	/**
	 * Runs a next Monte-Carlo-Simulation on a cached simulator. Returns the
	 * simulation result but does not cache it (as it is cached in the result of
	 * the simulator).
	 */
	@Rpc("next/simulation")
	public RpcResponse nextSimulation(RpcRequest req) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No simulator given", req);
		String id = Json.getString(req.params.getAsJsonObject(), "@id");
		if (id == null)
			return Responses.invalidParams(
				"No simulator with '@id' given", req);
		Object obj = context.cache.get(id);
		if (!(obj instanceof CachedResult))
			return Responses.invalidParams(
				"No cached simulator with @id=" + id, req);
		obj = ((CachedResult<?>) obj).result;
		if (!(obj instanceof Simulator))
			return Responses.invalidParams(
				"No cached simulator with @id=" + id, req);
		Simulator simulator = (Simulator) obj;
		SimpleResult r = simulator.nextRun();
		if (r == null)
			return Responses.internalServerError(
				"Simulation failed", req);
		JsonObject result = JsonRpc.encode(r, id, EntityCache.create(db));
		return Responses.ok(result, req);
	}

	@Rpc("calculate")
	public RpcResponse calculate(RpcRequest req) {
		var p = setupOf(req);
		if (p.second != null)
			return p.second;
		var setup = p.first;
		log.info("Calculate product system {}", setup.productSystem.refId);
		var type = Json.getEnum(
			req.params.getAsJsonObject(),
			"calculationType",
			CalculationType.class);
		if (type == null) {
			type = CalculationType.CONTRIBUTION_ANALYSIS;
			log.info("No calculation type defined; " +
				"calculate contributions as default");
		}
		return calculate(req, setup, type);
	}

	/**
	 * Builds a calculation setup from the given request. Returns an error
	 * response if this fails.
	 */
	private Pair<CalculationSetup, RpcResponse> setupOf(RpcRequest req) {

		Function<String, Pair<CalculationSetup, RpcResponse>> error = msg -> {
			var response = Responses.invalidParams(msg, req);
			return Pair.of(null, response);
		};

		if (req == null || req.params == null || !req.params.isJsonObject())
			return error.apply("No calculation setup given");
		var json = req.params.getAsJsonObject();

		// load the product system
		var systemID = Json.getRefId(json, "productSystem");
		if (systemID == null)
			return error.apply("No product system ID");
		var system = new ProductSystemDao(db).getForRefId(systemID);
		if (system == null)
			return error.apply("No product system found for @id=" + systemID);

		var setup = new CalculationSetup(system);

		// LCIA method and normalization and weighting
		var methodID = Json.getRefId(json, "impactMethod");
		if (Strings.notEmpty(methodID)) {
			setup.impactMethod = new ImpactMethodDao(db)
				.getDescriptorForRefId(methodID);
		}
		var nwSetID = Json.getRefId(json, "nwSet");
		if (Strings.notEmpty(nwSetID)) {
			setup.nwSet = new NwSetDao(db).getDescriptorForRefId(nwSetID);
		}

		// the quantitative reference
		setup.setAmount(Json.getDouble(json, "amount", system.targetAmount));
		var qref = system.referenceExchange;
		if (qref != null && qref.flow != null) {

			// flow property
			var propID = Json.getRefId(json, "flowProperty");
			if (Strings.notEmpty(propID)) {
				for (var f : qref.flow.flowPropertyFactors) {
					if (f.flowProperty != null
						&& !propID.equals(f.flowProperty.refId)) {
						setup.setFlowPropertyFactor(f);
						break;
					}
				}
			}

			// unit (we check for matching unit names and IDs here)
			var unitObj = Json.getObject(json, "unit");
			if (unitObj != null) {
				var prop = setup.getFlowPropertyFactor();
				var group = prop != null && prop.flowProperty != null
					? prop.flowProperty.unitGroup
					: null;
				var unitName = Json.getString(unitObj, "name");
				var unitID = Json.getString(unitObj, "@id");
				if (group != null && (Strings.notEmpty(unitName)
					|| Strings.notEmpty(unitID))) {
					group.units.stream()
						.filter(u -> Objects.equals(u.refId, unitID)
							|| Objects.equals(u.name, unitName))
						.findAny()
						.ifPresent(setup::setUnit);
				}
			}

		}

		// other calculation attributes
		setup.allocationMethod = Json.getEnum(
			json, "allocationMethod", AllocationMethod.class);
		setup.withCosts = Json.getBool(json, "withCosts", false);

		// add parameter redefinitions
		parameters(json, setup);

		return Pair.of(setup, null);
	}

	private void parameters(JsonObject json, CalculationSetup setup) {
		var array = Json.getArray(json, "parameterRedefs");
		if (array == null) {
			if (setup.productSystem != null) {
				var redefSet = setup.productSystem.parameterSets.stream()
					.filter(s -> s.isBaseline)
					.findAny();
				if (redefSet.isPresent()) {
					setup.parameterRedefs.clear();
					setup.parameterRedefs.addAll(redefSet.get().parameters);
				}
			}
			return;
		}
		for (var e : array) {
			if (!e.isJsonObject())
				continue;
			var obj = e.getAsJsonObject();
			var name = Json.getString(obj, "name");
			if (name == null)
				continue;
			var redef = new ParameterRedef();
			redef.name = name;
			redef.value = Json.getDouble(obj, "value", 1);

			var context = Json.getObject(obj, "context");
			if (context == null) {
				// global parameter redefinition
				setup.parameterRedefs.add(redef);
				continue;
			}

			// set the context
			var type = Json.getString(context, "@type");
			var refId = Json.getString(context, "@id");
			if (refId == null)
				continue;
			var d = "ImpactCategory".equals(type)
				? db.getDescriptor(ImpactCategory.class, refId)
				: db.getDescriptor(Process.class, refId);
			if (d == null)
				continue;
			redef.contextId = d.id;
			redef.contextType = d.type;
			setup.parameterRedefs.add(redef);
		}
	}

	private RpcResponse calculate(RpcRequest req, CalculationSetup setup,
								  CalculationType type) {
		try {
			var calc = new SystemCalculator(db, context.solver);
			SimpleResult r = null;
			switch (type) {
				case CONTRIBUTION_ANALYSIS:
					r = calc.calculateContributions(setup);
					break;
				case SIMPLE_CALCULATION:
					r = calc.calculateSimple(setup);
					break;
				case UPSTREAM_ANALYSIS:
					r = calc.calculateFull(setup);
					break;
				default:
					break;
			}
			if (r == null) {
				return Responses.error(501, "Calculation method " + type
					+ "is not yet implemented", req);
			}
			var id = UUID.randomUUID().toString();
			log.info("encode and cache result {}", id);
			context.cache.put(id, CachedResult.of(setup, r));
			var result = JsonRpc.encode(r, id, EntityCache.create(db));
			return Responses.ok(result, req);
		} catch (Exception e) {
			log.error("Calculation failed", e);
			return Responses.serverError(e, req);
		}
	}
}
