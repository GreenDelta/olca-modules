package org.openlca.git.writer;

import java.nio.charset.StandardCharsets;

import org.openlca.core.model.RefEntity;
import org.openlca.git.GitConfig;
import org.openlca.jsonld.output.JsonExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JsonWriter {

	private static final Logger log = LoggerFactory.getLogger(JsonWriter.class);

	private JsonWriter() {
	}

	static byte[] convert(RefEntity entity, GitConfig config) {
		if (entity == null)
			return null;
		try {
			var json = JsonExport.toJson(entity, config.database);
			return json.toString().getBytes(StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("failed to serialize " + entity, e);
			return null;
		}
	}


}
