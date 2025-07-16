package org.openlca.io.hestia;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record HestiaSource(JsonObject json) implements HestiaObject {

	public String name() {
		return Json.getString(json, "name");
	}

	public String originalLicense() {
		return Json.getString(json, "originalLicense");
	}


	public Bibliography bibliography() {
		var obj = Json.getObject(json, "bibliography");
		return obj != null
				? new Bibliography(obj)
				: null;
	}

	public record Bibliography(JsonObject json) {

		public String title() {
			return Json.getString(json, "title");
		}

		public String documentDOI() {
			return Json.getString(json, "documentDOI");
		}

		public String name() {
			return Json.getString(json, "name");
		}

		public String outlet() {
			return Json.getString(json, "outlet");
		}

		public Integer year() {
			var i = Json.getInt(json, "year");
			return i.isPresent()
					? i.getAsInt()
					: null;
		}

		public String mendeleyID() {
			return Json.getString(json, "mendeleyID");
		}

		public String scopus() {
			return Json.getString(json, "scopus");
		}

		public String articlePdf() {
			return Json.getString(json, "articlePdf");
		}
	}

}
