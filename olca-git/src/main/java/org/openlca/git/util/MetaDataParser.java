package org.openlca.git.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonToken;

public class MetaDataParser {

	private static final Logger log = LoggerFactory.getLogger(MetaDataParser.class);

	public static Map<String, Object> parse(InputStream json, String... fields) {
		var defs = Stream.of(fields).map(field -> new FieldDefinition(field)).toList();
		return parse(json, defs);
	}

	public static Map<String, Object> parse(InputStream json, List<FieldDefinition> fields) {
		if (fields == null || fields.size() == 0)
			return new HashMap<>();
		var matched = 0;
		try {
			var factory = new JsonFactory();
			var parser = factory.createParser(json);
			var parents = new ArrayList<String>();
			var map = new HashMap<String, Object>();
			String fieldName = null;
			var arrayDepth = 0;
			while ((matched < fields.size() || arrayDepth > 0) && !parser.isClosed()) {
				var jsonToken = parser.nextToken();
				if (JsonToken.START_ARRAY.equals(jsonToken)) {
					arrayDepth++;
					continue;
				}
				if (JsonToken.END_ARRAY.equals(jsonToken)) {
					arrayDepth--;
					continue;
				}
				if (JsonToken.START_OBJECT.equals(jsonToken)) {
					if (fieldName != null) {
						parents.add(fieldName);
					}
					continue;
				}
				if (JsonToken.END_OBJECT.equals(jsonToken)) {
					if (!parents.isEmpty()) {
						fieldName = parents.remove(parents.size() - 1);
					}
					continue;
				}
				if (!JsonToken.FIELD_NAME.equals(jsonToken))
					continue;
				fieldName = parser.getCurrentName();
				var match = findMatch(fields, parents, fieldName);
				if (match != null) {
					jsonToken = parser.nextToken();
					if (arrayDepth > 0) {
						@SuppressWarnings("unchecked")
						var list = (List<String>) map.computeIfAbsent(match.name, n -> new ArrayList<String>());
						list.add(parser.getValueAsString());
					} else {
						map.put(match.name, match.parser.apply(parser.getValueAsString()));
					}
					matched++;
				}
			}
			return map;
		} catch (IOException e) {
			log.error("Error parsing dataset", e);
			return new HashMap<>();
		}
	}

	private static FieldDefinition findMatch(List<FieldDefinition> candidates, List<String> parents, String field) {
		for (var candidate : candidates)
			if (isMatch(candidate, parents, field))
				return candidate;
		return null;
	}

	private static boolean isMatch(FieldDefinition candidate, List<String> parents, String field) {
		if (candidate.fields.length != parents.size() + 1)
			return false;
		for (var i = 0; i < candidate.fields.length - 1; i++)
			if (!candidate.fields[i].equals(parents.get(i)))
				return false;
		return candidate.fields[candidate.fields.length - 1].equals(field);
	}

}
