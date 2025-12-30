package org.openlca.io.pubchem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

/// Compound data that are returned by the PUG API.
public record PugCompound(JsonObject json) {

	public long id() {
		var top = Json.getObject(json, "id");
		var sub = Json.getObject(top, "id");
		var id = Json.getLong(sub, "cid");
		return id.isPresent()
			? id.getAsLong()
			: 0L;
	}

	public int charge() {
		return Json.getInt(json, "charge", 0);
	}

	public List<PugProp> properties() {
		var array = Json.getArray(json, "props");
		if (array == null)
			return List.of();
		var props = new ArrayList<PugProp>();
		for (var e : array) {
			if (e.isJsonObject()) {
				props.add(new PugProp(e.getAsJsonObject()));
			}
		}
		return props;
	}

	public String absoluteSmiles() {
		var v = getValue("SMILES", "Absolute");
		return v != null
			? v.getString()
			: null;
	}

	public String molecularFormula() {
		var v = getValue("Molecular Formula", null);
		return v != null
			? v.getString()
			: null;
	}

	public String connectivitySmiles() {
		var v = getValue("SMILES", "Connectivity");
		return v != null
			? v.getString()
			: null;
	}

	public String iupacNamePreferred() {
		var v = getValue("IUPAC Name", "Preferred");
		return v != null
			? v.getString()
			: null;
	}

	public String iupacNameSystematic() {
		var v = getValue("IUPAC Name", "Systematic");
		return v != null
			? v.getString()
			: null;
	}

	public String iupacNameTraditional() {
		var v = getValue("IUPAC Name", "Traditional");
		return v != null
			? v.getString()
			: null;
	}

	public String inchiString() {
		var v = getValue("InChI", "Standard");
		return v != null
			? v.getString()
			: null;
	}

	public String inchiKey() {
		var v = getValue("InChIKey", "Standard");
		return v != null
			? v.getString()
			: null;
	}

	private PugValue getValue(String label, String name) {
		for (var prop : properties()) {
			var urn = prop.urn();
			if (urn == null)
				continue;
			if (Objects.equals(label, urn.label())
				&& Objects.equals(name, urn.name()))
				return prop.value();
		}
		return null;
	}
}

