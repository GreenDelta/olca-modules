package org.openlca.jsonld.upgrades;

import org.openlca.core.model.ModelType;
import org.openlca.jsonld.JsonStoreReader;

import java.util.List;

/**
 * An upgrade updates the objects in a JSON store of on or more versions to a
 * specific target version. By default, it just delegates the calls to a
 * wrapped reader.
 */
abstract class Upgrade implements JsonStoreReader {

	protected final JsonStoreReader reader;

	Upgrade (JsonStoreReader reader) {
		this.reader = reader;
	}

	@Override
	public List<String> getRefIds(ModelType type) {
		return reader.getRefIds(type);
	}

	@Override
	public List<String> getFiles(String dir) {
		return reader.getFiles(dir);
	}

	@Override
	public byte[] getBytes(String path) {
		return reader.getBytes(path);
	}
}
