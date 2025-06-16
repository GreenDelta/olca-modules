package org.openlca.jsonld;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

import org.openlca.core.database.DataPackage;
import org.openlca.core.model.ModelType;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public interface JsonStoreWriter {

	/**
	 * Put the given json object of the given type into the store.
	 */
	default void put(ModelType type, JsonObject object) {
		if (type == null || object == null)
			return;
		var refId = Json.getString(object, "@id");
		var path = ModelPath.jsonOf(type, refId);
		put(path, object);
	}

	default void put(String path, JsonObject object) {
		if (path == null || object == null)
			return;
		var json = new Gson().toJson(object);
		var data = json.getBytes(StandardCharsets.UTF_8);
		put(path, data);
	}

	default void putBin(ModelType type, String refId, String filename, byte[] data) {
		var path = ModelPath.binFolderOf(type, refId) + "/" + filename;
		put(path, data);
	}

	default void putDataPackages(Collection<DataPackage> packages) {
		if (packages == null || packages.isEmpty())
			return;
		PackageInfo.create().withDataPackages(packages).writeTo(this);
	}

	void put(String path, byte[] data);

}
