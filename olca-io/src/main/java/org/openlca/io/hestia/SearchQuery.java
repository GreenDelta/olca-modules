package org.openlca.io.hestia;

import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public record SearchQuery(int limit, String term, boolean aggregated) {

	public SearchQuery(int limit, String term) {
		this(limit, term, true);
	}

	JsonObject toJson() {
		var root = new JsonObject();

		Json.put(root, "limit", limit);

		var fields = new JsonArray();
		fields.add("@id");
		fields.add("@type");
		fields.add("name");
		fields.add("termType");
		fields.add("aggregated");
		Json.put(root, "fields", fields);

		var boolObj = new JsonObject();
		Json.put(boolObj, "minimum_should_match", 1);
		addMustRule(boolObj);
		addMustNotRule(boolObj);
		addShouldRules(boolObj);

		var query = new JsonObject();
		query.add("bool", boolObj);
		root.add("query", query);

		return root;
	}

	private void addMustRule(JsonObject bool) {
		var typeObj = new JsonObject();
		Json.put(typeObj, "@type.keyword", "Cycle");
		var typeMatch = new JsonObject();
		Json.put(typeMatch, "match", typeObj);

		var array = new JsonArray();
		array.add(typeMatch);

		if (aggregated) {

			var qryObj = new JsonObject();
			Json.put(qryObj, "query", true);
			var aggObj = new JsonObject();
			Json.put(aggObj, "aggregated", qryObj);
			var aggMatch = new JsonObject();
			Json.put(aggMatch, "match", aggObj);
			array.add(aggMatch);

			var valObj = new JsonObject();
			Json.put(valObj, "aggregatedDataValidated", qryObj.deepCopy());
			var valMatch = new JsonObject();
			Json.put(valMatch, "match", valObj);
			array.add(valMatch);
		}

		Json.put(bool, "must", array);
	}

	private void addMustNotRule(JsonObject bool) {
		if (aggregated)
			return;
		var queryObj = new JsonObject();
		Json.put(queryObj, "query", true);
		var aggObj = new JsonObject();
		Json.put(aggObj, "aggregated", queryObj);
		var matchObj = new JsonObject();
		Json.put(matchObj, "match", aggObj);
		var array = new JsonArray();
		array.add(matchObj);
		Json.put(bool, "must_not", array);
	}

	private void addShouldRules(JsonObject boolObj) {
		var array = new JsonArray();
		array.add(shouldRuleOf("name.keyword", 10));
		array.add(shouldRuleOf("nameNormalized", 8));
		array.add(shouldRuleOf("nameSearchAsYouType", 2));
		boolObj.add("should", array);
	}

	private JsonObject shouldRuleOf(String field, int boost) {
		var queryObj = new JsonObject();
		queryObj.addProperty("query", term);
		queryObj.addProperty("boost", boost);
		var fieldObj = new JsonObject();
		fieldObj.add(field, queryObj);
		var matchObj = new JsonObject();
		matchObj.add("match", fieldObj);
		return matchObj;
	}
}
