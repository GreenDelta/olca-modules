package org.openlca.io.pubchem;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

/// A reference entry from the PUG View API response.
public record PugViewRef(JsonObject json) {

	public int referenceNumber() {
		return Json.getInt(json, "ReferenceNumber", 0);
	}

	public String sourceName() {
		return Json.getString(json, "SourceName");
	}

	public String sourceId() {
		return Json.getString(json, "SourceID");
	}

	public String name() {
		return Json.getString(json, "Name");
	}

	public String description() {
		return Json.getString(json, "Description");
	}

	public String url() {
		return Json.getString(json, "URL");
	}

	public String licenseNote() {
		return Json.getString(json, "LicenseNote");
	}

	public String licenseUrl() {
		return Json.getString(json, "LicenseURL");
	}

	public boolean isToxnet() {
		return Json.getBool(json, "IsToxnet", false);
	}

	public long anid() {
		var id = Json.getLong(json, "ANID");
		return id.isPresent() ? id.getAsLong() : 0;
	}
}
