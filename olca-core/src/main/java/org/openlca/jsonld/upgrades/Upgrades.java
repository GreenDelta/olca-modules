package org.openlca.jsonld.upgrades;

import org.openlca.jsonld.JsonStoreReader;
import org.openlca.jsonld.PackageInfo;
import org.openlca.jsonld.SchemaVersion;

public class Upgrades {

	public static JsonStoreReader chain(JsonStoreReader reader) {
		if (reader instanceof Upgrade)
			return reader;
		var version = PackageInfo.readFrom(reader)
				.schemaVersion()
				.value();
		if (version >= SchemaVersion.CURRENT)
			return reader;

		var chain = reader;

		if (version < 2) {
			chain = new Upgrade2(chain);
		}
		if (version < 3) {
			chain = new Upgrade3(chain);
		}
		if (version < 4) {
			chain = new Upgrade4(chain);
		}

		return chain;
	}
}
