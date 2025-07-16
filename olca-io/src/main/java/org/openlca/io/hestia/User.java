package org.openlca.io.hestia;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record User(JsonObject json) implements HestiaObject {

	public String firstName() {
		return Json.getString(json, "firstName");
	}

	public String lastName() {
		return Json.getString(json, "lastName");
	}

	public String displayName() {
		return Json.getString(json, "displayName");
	}

	public String orcid() {
		return Json.getString(json, "orcid");
	}

	public String website() {
		return Json.getString(json, "website");
	}

	public String city() {
		return Json.getString(json, "city");
	}

	public String country() {
		return Json.getString(json, "country");
	}

	public String primaryInstitution() {
		return Json.getString(json, "primaryInstitution");
	}

	public boolean dataPrivate() {
		return Json.getBool(json, "dataPrivate", false);
	}

	public boolean emailNotificationsSuccess() {
		return Json.getBool(json, "emailNotificationsSuccess", false);
	}

	public boolean emailNotificationsFailure() {
		return Json.getBool(json, "emailNotificationsFailure", false);
	}

	public boolean emailNotificationsFeedback() {
		return Json.getBool(json, "emailNotificationsFeedback", false);
	}

	public boolean autoSubmitPrivateSubmissions() {
		return Json.getBool(json, "autoSubmitPrivateSubmissions", false);
	}

	public String email() {
		return Json.getString(json, "email");
	}

	public String name() {
		return Json.getString(json, "name");
	}

	public String role() {
		return Json.getString(json, "role");
	}

	public String token() {
		return Json.getString(json, "token");
	}
}
