package org.openlca.jsonld.upgrades;

import org.openlca.jsonld.JsonStoreReader;
import org.openlca.jsonld.PackageInfo;

public class Upgrades {

	public static JsonStoreReader chain(JsonStoreReader reader) {
		if (reader instanceof Upgrade)
			return reader;
		var version = PackageInfo.readFrom(reader).schemaVersion();
		return version.value() < 2
			? new Upgrade2(reader)
			: reader;
	}
}
