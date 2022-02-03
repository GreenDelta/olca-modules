package org.openlca.ipc.handlers;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.math.Simulator;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.CalculationType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
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
		var simulator = Simulator.create(setup, db)
			.withSolver(context.solver);
		var id = UUID.randomUUID().toString();
		var obj = new JsonObject();
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
		return calculate(req, setup);
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

		// read the calculation type
		var type = Json.getEnum(json, "calculationType", CalculationType.class);
		if (type == null) {
			type = CalculationType.CONTRIBUTION_ANALYSIS;
			log.info("No calculation type defined; " +
				"calculate contributions as default");
		}

		var setup = new CalculationSetup(type, system);

		// LCIA method and normalization and weighting
		var methodID = Json.getRefId(json, "impactMethod");
		if (Strings.notEmpty(methodID)) {
			setup.withImpactMethod(db.get(ImpactMethod.class, methodID));
			var nwSetID = Json.getRefId(json, "nwSet");
			if (Strings.notEmpty(nwSetID) && setup.impactMethod() != null) {
				for (var nwSet : setup.impactMethod().nwSets) {
					if (nwSetID.equals(nwSet.refId)) {
						setup.withNwSet(nwSet);
						break;
					}
				}
			}
		}

		// the quantitative reference
		setup.withAmount(Json.getDouble(json, "amount", system.targetAmount));
		var qref = system.referenceExchange;
		if (qref != null && qref.flow != null) {

			// flow property
			var propID = Json.getRefId(json, "flowProperty");
			if (Strings.notEmpty(propID)) {
				for (var f : qref.flow.flowPropertyFactors) {
					if (f.flowProperty != null
						&& !propID.equals(f.flowProperty.refId)) {
						setup.withFlowPropertyFactor(f);
						break;
					}
				}
			}

			// unit (we check for matching unit names and IDs here)
			var unitObj = Json.getObject(json, "unit");
			if (unitObj != null) {
				var prop = setup.flowPropertyFactor();
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
						.ifPresent(setup::withUnit);
				}
			}
		}

		// other calculation attributes
		setup.withAllocation(
				Json.getEnum(json, "allocationMethod", AllocationMethod.class))
			.withCosts(Json.getBool(json, "withCosts", false));

		// add parameter redefinitions
		addParameters(json, setup);

		return Pair.of(setup, null);
	}

	private void addParameters(JsonObject json, CalculationSetup setup) {
		var array = Json.getArray(json, "parameterRedefs");

		// if no parameter redefinitions are defined, we take the redefinitions
		// of the baseline set by default
		if (array == null && setup.hasProductSystem()) {
			setup.productSystem().parameterSets.stream()
				.filter(s -> s.isBaseline)
				.findAny()
				.ifPresent(paramSet -> setup.withParameters(paramSet.parameters));
			return;
		}
		if (array == null)
			return;

		// add the redefinitions of the calculation setup
		var redefs = new ArrayList<ParameterRedef>();
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
				redefs.add(redef);
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
			redefs.add(redef);
		}

		setup.withParameters(redefs);
	}

	private RpcResponse calculate(RpcRequest req, CalculationSetup setup) {
		try {
			var calc = new SystemCalculator(db);
			SimpleResult r = switch (setup.type()) {
				case CONTRIBUTION_ANALYSIS -> calc.calculateContributions(setup);
				case SIMPLE_CALCULATION -> calc.calculateSimple(setup);
				case UPSTREAM_ANALYSIS -> calc.calculateFull(setup);
				default -> null;
			};
			if (r == null) {
				return Responses.error(501,
					"invalid calculation type: " + setup.type(), req);
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
