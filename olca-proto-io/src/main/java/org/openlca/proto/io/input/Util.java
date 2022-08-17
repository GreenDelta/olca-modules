package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

class Util {

	private Util() {
	}

	static void mapBase(RootEntity e, ProtoWrap proto, EntityResolver resolver) {
		if (proto == null)
			return;
		e.refId = proto.id();
		e.name = proto.name();
		e.description = proto.description();
		e.version = parseVersion(proto);
		e.lastChange = parseLastChange(proto);

		// category
		var path = proto.category();
		if (Strings.notEmpty(path)) {
			var type = ModelType.of(e);
			e.category = resolver.getCategory(type, path);
		}

		// tags
		if(!proto.tags().isEmpty()) {
			e.tags = String.join(",", proto.tags());
		}
	}

	private static long parseVersion(ProtoWrap proto) {
		var s = proto.version();
		return Strings.nullOrEmpty(s)
			? 0
			: Version.fromString(s).getValue();
	}

	private static long parseLastChange(ProtoWrap proto) {
		var s = proto.lastChange();
		if (Strings.nullOrEmpty(s))
			return 0;
		var date = Json.parseDate(s);
		return date != null
			? date.getTime()
			: 0;
	}
}
