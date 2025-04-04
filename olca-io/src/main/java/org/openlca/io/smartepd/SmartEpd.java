package org.openlca.io.smartepd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record SmartEpd(JsonObject json) {

	public SmartEpd() {
		this(new JsonObject());
	}

	public SmartEpd(JsonObject json) {
		this.json = Objects.requireNonNull(json);
	}

	public static Optional<SmartEpd> of(JsonElement e) {
		return e != null && e.isJsonObject()
				? Optional.of(new SmartEpd(e.getAsJsonObject()))
				: Optional.empty();
	}

	public static List<SmartEpd> allOf(JsonArray array) {
		var epds = new ArrayList<SmartEpd>();
		for (var e : array) {
			of(e).ifPresent(epds::add);
		}
		return epds;
	}

	public String project() {
		return Json.getString(json, "project");
	}

	public SmartEpd project(String projectId) {
		Json.put(json, "project", projectId);
		return this;
	}

	public String id() {
		return Json.getString(json, "id");
	}

	public SmartEpd id(String id) {
		Json.put(json, "id", id);
		return this;
	}

	public String productName() {
		return Json.getString(json, "product_name");
	}

	public SmartEpd productName(String productName) {
		Json.put(json, "product_name", productName);
		return this;
	}

	public String productDescription() {
		return Json.getString(json, "product_description");
	}

	public SmartEpd productDescription(String productDescription) {
		Json.put(json, "product_description", productDescription);
		return this;
	}

	public String scope() {
		return Json.getString(json, "scope");
	}

	public SmartEpd scope(String scope) {
		Json.put(json, "scope", scope);
		return this;
	}

	public String scopeDescription() {
		return Json.getString(json, "scope_description");
	}

	public SmartEpd scopeDescription(String scopeDescription) {
		Json.put(json, "scope_description", scopeDescription);
		return this;
	}

	public SmartRefUnit refUnit() {
		var type = Json.getInt(json, "unit_type", 1);
		var key = type == SmartRefUnit.FUNCTIONAL
				? "functional_unit"
				: "declared_unit";
		return SmartRefUnit
				.of(Json.getObject(json, key))
				.orElse(null);
	}

	public SmartEpd declaredUnit(SmartRefUnit u) {
		Json.put(json, "unit_type", SmartRefUnit.DECLARED);
		if (u != null) {
			Json.put(json, "declared_unit", u.json());
		}
		return this;
	}

	public SmartEpd functionalUnit(SmartRefUnit u) {
		Json.put(json, "unit_type", SmartRefUnit.FUNCTIONAL);
		if (u != null) {
			Json.put(json, "functional_unit", u.json());
		}
		return this;
	}

	public double massPerUnit() {
		return Json.getDouble(json, "mass_per_functional_unit", 0);
	}

	public SmartEpd massPerUnit(double massPerUnit) {
		Json.put(json, "mass_per_functional_unit", massPerUnit);
		return this;
	}

	public String lcaSoftware() {
		return Json.getString(json, "lca_software");
	}

	public SmartEpd lcaSoftware(String lcaSoftware) {
		Json.put(json, "lca_software", lcaSoftware);
		return this;
	}

	public String lcaSoftwareVersion() {
		return Json.getString(json, "lca_software_version");
	}

	public SmartEpd lcaSoftwareVersion(String lcaSoftwareVersion) {
		Json.put(json, "lca_software_version", lcaSoftwareVersion);
		return this;
	}

	public String lifeCycleStageDescription() {
		return Json.getString(json, "life_cycle_stage_description");
	}

	public SmartEpd lifeCycleStageDescription(String lifeCycleStageDescription) {
		Json.put(json, "life_cycle_stage_description", lifeCycleStageDescription);
		return this;
	}

	public String lcaAllocationProcedure() {
		return Json.getString(json, "lca_allocation_procedure");
	}

	public SmartEpd lcaAllocationProcedure(String lcaAllocationProcedure) {
		Json.put(json, "lca_allocation_procedure", lcaAllocationProcedure);
		return this;
	}

	public String lcaCutOffProcedure() {
		return Json.getString(json, "lca_cut_off_procedure");
	}

	public SmartEpd lcaCutOffProcedure(String lcaCutOffProcedure) {
		Json.put(json, "lca_cut_off_procedure", lcaCutOffProcedure);
		return this;
	}

	public String lcaDataQualityDiscussion() {
		return Json.getString(json, "lca_data_quality_discussion");
	}

	public SmartEpd lcaDataQualityDiscussion(String lcaDataQualityDiscussion) {
		Json.put(json, "lca_data_quality_discussion", lcaDataQualityDiscussion);
		return this;
	}

	public String interpretation() {
		return Json.getString(json, "interpretation");
	}

	public SmartEpd interpretation(String interpretation) {
		Json.put(json, "interpretation", interpretation);
		return this;
	}

	public String references() {
		return Json.getString(json, "references");
	}

	public SmartEpd references(String references) {
		Json.put(json, "references", references);
		return this;
	}

	public String limitations() {
		return Json.getString(json, "limitations");
	}

	public SmartEpd limitations(String limitations) {
		Json.put(json, "limitations", limitations);
		return this;
	}

	public List<SmartResultList> resultListsOf(SmartIndicatorType type) {
		var array = Json.getArray(json, listKeyOf(type));
		return SmartResultList.allOf(array);
	}

	public List<SmartResult> resultsOf(SmartIndicatorType type) {
		var array = Json.getArray(json, keyOf(type));
		if ( array != null )
			return SmartResult.allOf(array);
		var lists = resultListsOf(type);
		return !lists.isEmpty()
				? lists.getFirst().results()
				: List.of();
	}

	/// In the SmartEPD format, results can be written as lists and lists of
	/// lists. It is not always clear what is allowed and required when. This
	/// method writes both, the results and the lists of results.
	public void putResultsAndLists(
			SmartIndicatorType type, List<SmartResult> results
	) {
		if (type == null || results == null)
			return;
		putResults(type, results);
		var list = new SmartResultList(type).results(results);
		putResultLists(type, List.of(list));
	}

	public void putResults(SmartIndicatorType type, List<SmartResult> results) {
		if (type == null || results == null)
			return;
		var array = new JsonArray(results.size());
		for (var result : results) {
			array.add(result.json());
		}
		Json.put(json, keyOf(type), array);
	}

	public void putResultLists(
			SmartIndicatorType type, List<SmartResultList> lists
	) {
		if (type == null || lists == null)
			return;
		var array = new JsonArray(lists.size());
		for (var result : lists) {
			array.add(result.json());
		}
		Json.put(json, listKeyOf(type), array);
	}

	private String listKeyOf(SmartIndicatorType type) {
		if (type == null)
			return "null";
		return switch (type) {
			case IMPACT -> "impacts_list";
			case RESOURCE -> "resource_uses_list";
			case OUTPUT -> "output_flows_list";
		};
	}

	private String keyOf(SmartIndicatorType type) {
		if (type == null)
			return "null";
		return switch (type) {
			case IMPACT -> "impacts";
			case RESOURCE -> "resource_uses";
			case OUTPUT -> "output_flows";
		};
	}

}
