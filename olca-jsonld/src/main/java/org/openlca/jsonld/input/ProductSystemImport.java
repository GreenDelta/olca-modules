package org.openlca.jsonld.input;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Scenario;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ProductSystemImport extends BaseImport<ProductSystem> {

	private ProductSystemImport(String refId, ImportConfig conf) {
		super(ModelType.PRODUCT_SYSTEM, refId, conf);
	}

	static ProductSystem run(String refId, ImportConfig conf) {
		return new ProductSystemImport(refId, conf).run();
	}

	@Override
	ProductSystem map(JsonObject json, long id) {
		if (json == null)
			return null;
		ProductSystem s = new ProductSystem();
		In.mapAtts(json, s, id, conf);
		String processRefId = Json.getRefId(json, "referenceProcess");
		if (processRefId != null) {
			s.referenceProcess = ProcessImport.run(processRefId, conf);
		}

		s.targetAmount = Json.getDouble(json, "targetAmount", 1d);
		addProcesses(json, s);
		addInventory(json, s);
		addScenarios(json, s);
		importLinkRefs(json, s);
		ProductSystemLinks.map(json, conf, s);

		// add parameter redefinitions
		JsonArray redefs = Json.getArray(json, "parameterRedefs");
		if (redefs != null) {
			s.parameterRedefs.addAll(
					ParameterRedefs.read(redefs, conf));
		}

		return conf.db.put(s);
	}

	private void addProcesses(JsonObject json, ProductSystem s) {
		JsonArray array = Json.getArray(json, "processes");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement e : array) {
			if (e.isJsonObject()) {
				addProcess(s, e.getAsJsonObject());
			}
		}
	}

	private void addProcess(ProductSystem s, JsonObject ref) {
		if (ref == null)
			return;
		String refId = Json.getString(ref, "@id");
		String type = Json.getString(ref, "@type");
		RootEntity p = null;
		if ("ProductSystem".equals(type)) {
			p = ProductSystemImport.run(refId, conf);
		} else {
			p = ProcessImport.run(refId, conf);
			if (p == null) {
				p = ProductSystemImport.run(refId, conf);
			}
		}
		if (p != null) {
			s.processes.add(p.id);
		}
	}

	private void importLinkRefs(JsonObject json, ProductSystem s) {
		JsonArray array = Json.getArray(json, "processLinks");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement element : array) {
			JsonObject obj = element.getAsJsonObject();
			String flowRefId = Json.getRefId(obj, "flow");
			FlowImport.run(flowRefId, conf);
			addProcess(s, Json.getObject(obj, "provider"));
			addProcess(s, Json.getObject(obj, "process"));
		}
	}

	private void addInventory(JsonObject json, ProductSystem s) {
		JsonArray array = Json.getArray(json, "inventory");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement element : array) {
			JsonObject ref = element.getAsJsonObject();
			Exchange ex = ExchangeImport.run(ModelType.PRODUCT_SYSTEM, s.refId,
					ref, conf,
					(ProductSystem system) -> system.inventory);
			s.inventory.add(ex);
		}
	}

	private void addScenarios(JsonObject json, ProductSystem s) {
		JsonArray array = Json.getArray(json, "scenarios");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement elem : array) {
			if (!elem.isJsonObject())
				continue;
			JsonObject obj = elem.getAsJsonObject();
			Scenario scenario = new Scenario();
			s.scenarios.add(scenario);
			scenario.name = Json.getString(obj, "name");
			scenario.description = Json.getString(obj, "description");
			scenario.isBaseline = Json.getBool(obj, "isBaseline", false);
			JsonArray redefs = Json.getArray(obj, "parameters");
			if (redefs != null && redefs.size() > 0) {
				scenario.parameters.addAll(
						ParameterRedefs.read(redefs, conf));
			}
		}
	}
}
