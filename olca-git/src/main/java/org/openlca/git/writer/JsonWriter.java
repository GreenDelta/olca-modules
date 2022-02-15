package org.openlca.git.writer;

import java.nio.charset.StandardCharsets;

import org.openlca.core.model.RootEntity;
import org.openlca.git.Config;
import org.openlca.jsonld.output.JsonExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JsonWriter {

	private static final Logger log = LoggerFactory.getLogger(JsonWriter.class);

	private JsonWriter() {
	}

	static byte[] convert(RootEntity entity, Config config) {
		if (entity == null)
			return null;
		try {
			var json = JsonExport.toJson(entity);
			return json.toString().getBytes(StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("failed to serialize " + entity, e);
			return null;
		}
	}


}
