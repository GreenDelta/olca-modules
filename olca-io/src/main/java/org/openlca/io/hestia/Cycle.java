package org.openlca.io.hestia;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openlca.io.hestia.HestiaExchange.Emission;
import org.openlca.io.hestia.HestiaExchange.Input;
import org.openlca.io.hestia.HestiaExchange.Product;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record Cycle(JsonObject json) implements HestiaObject {

	public String name() {
		return Json.getString(json, "name");
	}

	public String description() {
		return Json.getString(json, "description");
	}

	public String functionalUnit() {
		return Json.getString(json, "functionalUnit");
	}

	public Date createdAt() {
		return Json.getDate(json, "createdAt");
	}

	public Date updatedAt() {
		return Json.getDate(json, "updatedAt");
	}

	public Date endDate() {
		return Json.getDate(json, "endDate");
	}

	public Date startDate() {
		return Json.getDate(json, "startDate");
	}

	public Site site() {
		var obj = Json.getObject(json, "site");
		if (obj == null)
			return null;
		return new Site(obj);
	}

	public List<Input> inputs() {
		var inputs = new ArrayList<Input>();
		Json.forEachObject(json, "inputs", obj -> {
			var input = new Input(obj);
			inputs.add(input);
		});
		return inputs;
	}

	public List<Product> products() {
		var products = new ArrayList<Product>();
		Json.forEachObject(json, "products", obj -> {
			var product = new Product(obj);
			products.add(product);
		});
		return products;
	}

	public List<Emission> emissions() {
		var emissions = new ArrayList<Emission>();
		Json.forEachObject(json, "emissions", obj -> {
			var emission = new Emission(obj);
			emissions.add(emission);
		});
		return emissions;
	}

	public List<Practice> practices() {
		var practices = new ArrayList<Practice>();
		Json.forEachObject(json, "practices", obj -> {
			var practice = new Practice(obj);
			practices.add(practice);
		});
		return practices;
	}
}
