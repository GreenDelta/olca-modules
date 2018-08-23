package org.openlca.ipc.handlers;

import java.util.UUID;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.NwSetDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.CalculationType;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.results.SimpleResult;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Calculator {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final HandlerContext context;
	private final IDatabase db;

	public Calculator(HandlerContext context) {
		this.context = context;
		this.db = context.db;
	}

	@Rpc("calculate")
	public RpcResponse calculate(RpcRequest req) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No calculation setup given", req);
		JsonObject json = req.params.getAsJsonObject();
		String systemID = Json.getRefId(json, "productSystem");
		if (systemID == null)
			Responses.invalidParams("No product system ID", req);
		ProductSystem system = new ProductSystemDao(db).getForRefId(systemID);
		if (system == null)
			Responses.invalidParams(
					"No product system found for @id=" + systemID, req);
		log.info("Calculate product system {}", systemID);
		CalculationSetup setup = buildSetup(json, system);
		return calculate(req, setup);
	}

	private CalculationSetup buildSetup(JsonObject json, ProductSystem system) {
		CalculationType type = Json.getEnum(json, "calculationType",
				CalculationType.class);
		if (type == null) {
			type = CalculationType.CONTRIBUTION_ANALYSIS;
			log.info("No calculation type defined; " +
					"calculate contributions as default");
		}
		CalculationSetup setup = new CalculationSetup(type, system);
		String methodID = Json.getRefId(json, "impactMethod");
		if (methodID != null) {
			setup.impactMethod = new ImpactMethodDao(db)
					.getDescriptorForRefId(methodID);
		}
		String nwSetID = Json.getRefId(json, "nwSet");
		if (nwSetID != null) {
			setup.nwSet = new NwSetDao(db).getDescriptorForRefId(nwSetID);
		}
		setup.withCosts = Json.getBool(json, "withCosts", false);
		setup.setAmount(Json.getDouble(json, "amount", system.targetAmount));
		parameters(json, setup);
		return setup;
	}

	private void parameters(JsonObject json, CalculationSetup setup) {
		JsonArray array = Json.getArray(json, "parameterRedefs");
		if (array == null)
			return;
		for (JsonElement e : array) {
			if (!e.isJsonObject())
				continue;
			JsonObject obj = e.getAsJsonObject();
			String name = Json.getString(obj, "name");
			if (name == null)
				continue;
			ParameterRedef redef = new ParameterRedef();
			redef.setName(name);
			redef.setValue(Json.getDouble(obj, "value", 1));

			JsonObject context = Json.getObject(obj, "context");
			if (context == null) {
				// global parameter redefinition
				setup.parameterRedefs.add(redef);
				continue;
			}

			// set the context
			BaseDescriptor d = parameterContext(context);
			if (d == null)
				continue;
			redef.setContextId(d.getId());
			redef.setContextType(d.getModelType());
			setup.parameterRedefs.add(redef);
		}
	}

	private BaseDescriptor parameterContext(JsonObject context) {
		String type = Json.getString(context, "@type");
		String refId = Json.getString(context, "@id");
		if ("Process".equals(type)) {
			return new ProcessDao(db).getDescriptorForRefId(refId);
		} else if ("ImpactMethod".equals(type)) {
			return new ImpactMethodDao(db).getDescriptorForRefId(refId);
		}
		return null;
	}

	private RpcResponse calculate(RpcRequest req, CalculationSetup setup) {
		try {
			SystemCalculator calc = new SystemCalculator(
					MatrixCache.createEager(db), context.solver);
			SimpleResult r = null;
			switch (setup.type) {
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
				return Responses.error(501, "Calculation method " + setup.type
						+ "is not yet implemented", req);
			}
			String id = UUID.randomUUID().toString();
			log.info("encode and cache result {}", id);
			context.cache.put(id, CachedResult.of(setup, r));
			JsonObject result = JsonRpc.encode(r, id, EntityCache.create(db));
			return Responses.ok(result, req);
		} catch (Exception e) {
			log.error("Calculation failed", e);
			return Responses.serverError(e, req);
		}
	}
}
