package org.openlca.git.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.openlca.git.util.FieldDefinition.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class MetaDataParser {

	private static final Logger log = LoggerFactory.getLogger(MetaDataParser.class);
	private final JsonParser parser;
	private final List<FieldDefinition> defs;
	private final Map<String, Object> values = new HashMap<>();
	private final Map<String, Object> candidates = new HashMap<>();
	private final Set<String> metCondition = new HashSet<>();

	private MetaDataParser(InputStream json, List<FieldDefinition> defs) throws IOException {
		this.parser = new JsonFactory().createParser(json);
		this.defs = new ArrayList<>(defs);
	}

	public static Map<String, Object> parse(InputStream json, String... fields) {
		var defs = Stream.of(fields).map(field -> FieldDefinition.firstOf(field)).toList();
		return parse(json, defs);
	}

	public static Map<String, Object> parse(InputStream json, FieldDefinition... defs) {
		return parse(json, defs != null ? Arrays.asList(defs) : null);		
	}

	public static Map<String, Object> parse(InputStream json, List<FieldDefinition> defs) {
		if (defs == null || defs.size() == 0)
			return new HashMap<>();
		try {
			var instance = new MetaDataParser(json, defs);
			instance.parse();
			return instance.values;
		} catch (IOException e) {
			log.error("Error parsing dataset", e);
			return new HashMap<>();
		}
	}

	private void parse() throws IOException {
		var fields = new ArrayList<String>();
		String current = null;
		while (!parser.isClosed() && !defs.isEmpty()) {
			var token = parser.nextToken();
			if (JsonToken.START_ARRAY.equals(token)) {

			} else if (JsonToken.END_ARRAY.equals(token)) {
				for (var def : new ArrayList<>(defs)) {
					var c = join(fields, current);
					if (def.type == Type.ALL && startMatches(def.fields, c)
							&& (!def.isConditional() || startMatches(def.conditionFields, c))) {
						values.put(def.name, values.get(def.name));
						defs.remove(def);
					}
				}
			} else if (JsonToken.START_OBJECT.equals(token)) {
				if (current != null) {
					fields.add(current);
				}
				for (var def : defs) {
					if (def.isConditional() && startMatches(def.conditionFields, fields)) {
						metCondition.remove(def.name);
						candidates.remove(def.name);
					}
				}
			} else if (JsonToken.END_OBJECT.equals(token)) {
				if (!fields.isEmpty()) {
					current = fields.remove(fields.size() - 1);
				}
			} else if (JsonToken.FIELD_NAME.equals(token)) {
				current = parser.getCurrentName();
			} else if (current != null) {
				handleValue(join(fields, current));
			}
		}
	}

	private void handleValue(List<String> fields) throws IOException {
		String value = null;
		for (var def : new ArrayList<>(defs)) {
			if (isExactMatch(def.fields, fields)) {
				value = value != null ? value : parser.getValueAsString();
				handleValue(def, value);
			} else if (def.isConditional() && isExactMatch(def.conditionFields, fields)) {
				value = value != null ? value : parser.getValueAsString();
				if (def.meetsCondition.apply(value)) {
					metCondition.add(def.name);
					var candidate = candidates.get(def.name);
					if (candidate != null) {
						if (def.type == Type.FIRST) {
							values.put(def.name, candidate);
							defs.remove(def);
						} else {
							values.put(def.name, join(toList(values.get(def.name)), candidate));
						}
					}
				}
			}
		}
	}

	private void handleValue(FieldDefinition def, String v) throws IOException {
		var value = def.converter.apply(v);
		var conditionWasMet = !def.isConditional() || metCondition.contains(def.name);
		switch (def.type) {
			case FIRST -> {
				if (!conditionWasMet) {
					candidates.put(def.name, value);
				} else {
					values.put(def.name, value);
					defs.remove(def);
				}
			}
			case ALL -> {
				if (!conditionWasMet) {
					candidates.put(def.name, value);
				} else {
					values.put(def.name, join(toList(values.get(def.name)), value));
				}
			}
		}
	}

	private static boolean isExactMatch(List<String> fields, List<String> otherFields) {
		return isMatch(fields, otherFields, true);
	}

	private static boolean startMatches(List<String> fields, List<String> otherFields) {
		return isMatch(fields, otherFields, false);
	}

	private static boolean isMatch(List<String> fields, List<String> otherFields, boolean exact) {
		if (exact && fields.size() != otherFields.size())
			return false;
		if (fields.size() < otherFields.size())
			return false;
		for (var i = 0; i < otherFields.size(); i++)
			if (!fields.get(i).equals(otherFields.get(i)))
				return false;
		return true;
	}

	private static <T> List<T> join(List<T> current, T newElement) {
		var joined = new ArrayList<T>(current);
		joined.add(newElement);
		return joined;
	}

	@SuppressWarnings("unchecked")
	private static List<Object> toList(Object value) {
		if (value == null)
			return new ArrayList<>();
		return (List<Object>) value;
	}

}
