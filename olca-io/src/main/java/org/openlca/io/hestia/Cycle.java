package org.openlca.io.hestia;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
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
		var s = Json.getString(json, "endDate");
		try {
			var year = Integer.parseInt(s);
			var instant = LocalDate.of(year, Month.DECEMBER, 31)
					.atStartOfDay(ZoneId.systemDefault())
					.toInstant();
			return Date.from(instant);
		} catch (Exception ignored) {
		}
		return Json.parseDate(s);
	}

	public Date startDate() {
		var s = Json.getString(json, "startDate");
		try {
			var year = Integer.parseInt(s);
			var instant = LocalDate.of(year, Month.JANUARY, 1)
					.atStartOfDay(ZoneId.systemDefault())
					.toInstant();
			return Date.from(instant);
		} catch (Exception ignored) {
		}
		return Json.parseDate(s);
	}

	public HestiaRef site() {
		var obj = Json.getObject(json, "site");
		return obj != null
				? new HestiaRef(obj)
				: null;
	}

	public HestiaRef defaultSource() {
		var obj = Json.getObject(json, "defaultSource");
		return obj != null
				? new HestiaRef(obj)
				: null;
	}

	public List<HestiaRef> aggregatedSources() {
		var refs = new ArrayList<HestiaRef>();
		Json.forEachObject(json, "aggregatedSources",
				obj -> refs.add(new HestiaRef(obj)));
		return refs;
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
