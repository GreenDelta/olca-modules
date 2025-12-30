package org.openlca.io.pubchem;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.openlca.commons.Res;
import org.openlca.commons.Strings;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/// A client for the PubChem PUG REST API.
///
/// Example URLs for ethanol (CID 702):
///
/// - [Compound by name](https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/ethanol/JSON)
/// - [Compound view](https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/data/compound/702/JSON)
///
public class PubChemClient implements AutoCloseable {

	private final HttpClient http;

	public PubChemClient() {
		this.http = HttpClient.newHttpClient();
	}

	public Res<List<PugCompound>> getCompoundsByName(String name) {
		if (Strings.isBlank(name))
			return Res.error("empty compound name provided");

		try {
			var encodedName = URLEncoder.encode(name.strip(), StandardCharsets.UTF_8)
				.replace("+", "%20");
			var path = "/compound/name/" + encodedName + "/JSON";

			var json = getJsonObject(path);
			if (json.isError())
				return json.wrapError("failed to get compounds for: " + name);

			var array = Json.getArray(json.value(), "PC_Compounds");
			if (array == null)
				return Res.error("response does not contain PC_Compounds array");

			var compounds = new ArrayList<PugCompound>();
			for (var e : array) {
				if (e.isJsonObject()) {
					compounds.add(new PugCompound(e.getAsJsonObject()));
				}
			}
			return Res.ok(compounds);
		} catch (Exception e) {
			return Res.error("request failed for compound: " + name, e);
		}
	}

	public Res<PugView> getCompoundView(long compoundId) {
		try {
			var path = "_view/data/compound/" + compoundId + "/JSON";
			var json = getJsonObject(path);
			if (json.isError())
				return json.wrapError("failed to get compound view for: " + compoundId);

			var record = Json.getObject(json.value(), "Record");
			if (record == null)
				return Res.error("response does not contain Record object");

			return Res.ok(new PugView(record));
		} catch (Exception e) {
			return Res.error("request failed for compound view: " + compoundId, e);
		}
	}

	private Res<JsonObject> getJsonObject(String path) {
		try {
			var base = "https://pubchem.ncbi.nlm.nih.gov/rest/pug";
			var req = HttpRequest.newBuilder()
				.uri(URI.create(base + path))
				.header("accept", "application/json")
				.build();

			var resp = http.send(req, BodyHandlers.ofString());
			if (resp.statusCode() != 200) {
				return Res.error("request failed: "
					+ resp.statusCode() + " - " + resp.body());
			}
			var json = JsonParser.parseString(resp.body());
			return json.isJsonObject()
				? Res.ok(json.getAsJsonObject())
				: Res.error("response is not a JSON object");
		} catch (Exception e) {
			return Res.error("request failed", e);
		}
	}

	@Override
	public void close() {
		http.close();
	}
}
