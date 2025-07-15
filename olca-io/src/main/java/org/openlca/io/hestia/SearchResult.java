package org.openlca.io.hestia;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record SearchResult(JsonObject json) implements HestiaObject {

    public String name() {
        return Json.getString(json, "name");
    }

    public double score() {
        return Json.getDouble(json, "_score", 0.0);
    }
}
