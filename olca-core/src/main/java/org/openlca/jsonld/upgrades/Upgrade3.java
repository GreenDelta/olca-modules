package org.openlca.jsonld.upgrades;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.JsonStoreReader;
import org.openlca.commons.Strings;

class Upgrade3 extends Upgrade {

	Upgrade3(JsonStoreReader reader) {
		super(reader);
	}

	@Override
	public JsonObject get(ModelType type, String refId) {
		var obj = super.get(type, refId);
		if (obj == null)
			return null;
		if (type == ModelType.PROCESS) {
			upgradeProcessReview(obj);
		}
		return obj;
	}

	private void upgradeProcessReview(JsonObject obj) {
		var doc = Json.getObject(obj, "processDocumentation");
		if (doc == null)
			return;
		var reviewer = Json.getObject(doc, "reviewer");
		var details = Json.getString(doc, "reviewDetails");
		if (reviewer == null && Strings.isBlank(details))
			return;

		var review = new JsonObject();
		Json.put(review, "details", details);
		if (reviewer != null) {
			var reviewers = new JsonArray(1);
			reviewers.add(		reviewer.deepCopy());
			Json.put(review, "reviewers", reviewers);
		}
		var reviews = new JsonArray(1);
		reviews.add(review);
		Json.put(doc, "reviews", reviews);
	}
}
