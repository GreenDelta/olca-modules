package org.openlca.io.smartepd;

import java.util.Optional;

import org.openlca.jsonld.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record SmartOrg(JsonObject json) {

	public static Optional<SmartOrg> of(JsonElement e) {
		if (e == null || !e.isJsonObject())
			return Optional.empty();
		var obj = e.getAsJsonObject();
		return Optional.of(new SmartOrg(obj));
	}

	public String webDomain() {
		return Json.getString(json, "web_domain");
	}

	public SmartOrg webDomain(String webDomain) {
		Json.put(json, "web_domain", webDomain);
		return this;
	}

	public String name() {
		return Json.getString(json, "name");
	}

	public SmartOrg name(String name) {
		Json.put(json, "name", name);
		return this;
	}

	public String address() {
		return Json.getString(json, "address");
	}

	public SmartOrg address(String address) {
		Json.put(json, "address", address);
		return this;
	}
}
