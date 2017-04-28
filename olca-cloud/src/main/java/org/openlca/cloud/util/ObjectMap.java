package org.openlca.cloud.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ObjectMap extends HashMap<String, Object> {

	private static final long serialVersionUID = 8510487677972200938L;
	private static final Gson mapper = new Gson();

	public ObjectMap() {
		this(new HashMap<>());
	}

	public static ObjectMap fromJson(String json) {
		return new ObjectMap(toMap(json));
	}

	public static ObjectMap fromMap(Map<String, Object> managed) {
		return new ObjectMap(managed);
	}

	public static ObjectMap fromObject(Object object) {
		if (object == null)
			return null;
		String json = mapper.toJson(object);
		return fromJson(json);
	}

	private ObjectMap(Map<String, Object> managed) {
		if (managed != null)
			putAll(managed);
	}

	private static Map<String, Object> toMap(String json) {
		if (json == null)
			return null;
		return mapper.fromJson(json, new TypeToken<Map<String, Object>>() {
		}.getType());
	}

	public void removeAllBut(String... fields) {
		if (fields == null)
			return;
		Set<String> fieldSet = new HashSet<>();
		for (String field : fields)
			if (field.contains("."))
				throw new IllegalArgumentException("removeAllBut doesn't support complex fields");
			else
				fieldSet.add(field);
		for (String key : new HashSet<>(keySet()))
			if (!fieldSet.contains(key))
				remove(key);
	}

	@Override
	public boolean remove(Object key, Object value) {
		if (key instanceof String && value instanceof String) {
			remove(new String[] { key.toString(), value.toString() });
			return true;
		} else
			return super.remove(key, value);
	}

	public void remove(String... fields) {
		if (fields == null)
			return;
		for (String field : fields)
			remove(field);
	}

	@Override
	public Object remove(Object field) {
		return remove(this, field != null ? field.toString() : null);
	}

	private Object remove(Map<String, Object> map, String field) {
		if (map == null)
			return null;
		if (field.contains(".")) {
			String prefix = field.substring(0, field.lastIndexOf('.'));
			field = field.substring(field.lastIndexOf('.') + 1);
			Collection<Object> allNext = getAll(map, prefix);
			List<Object> previous = new ArrayList<>();
			for (Object next : allNext)
				if (next instanceof Map)
					previous.add(((Map<?, ?>) next).remove(field));
			return previous;
		} else if (map == this)
			return super.remove(field);
		else
			return map.remove(field);
	}

	@SuppressWarnings("unchecked")
	private Collection<Object> toArray(Object value) {
		if (value == null)
			return Collections.emptyList();
		if (value instanceof Collection)
			return ((Collection<Object>) value);
		return Collections.singletonList(value);
	}

	@Override
	public Object put(String field, Object value) {
		Map<String, Object> map = this;
		if (field.contains(".")) {
			String prefix = field.substring(0, field.lastIndexOf('.'));
			field = field.substring(field.lastIndexOf('.') + 1);
			map = get(map, prefix, true, true);
		}
		if (map == this)
			return super.put(field, value);
		else
			return map.put(field, value);
	}

	public void nullify(String... fields) {
		if (fields == null)
			return;
		for (String field : fields)
			put(field, null);
	}

	public void removeEmptyOrNull() {
		removeEmptyOrNull(this);
	}

	@SuppressWarnings("unchecked")
	private void removeEmptyOrNull(Map<String, Object> map) {
		for (String key : new HashSet<>(map.keySet())) {
			Object value = map.get(key);
			if (map.get(key) == null)
				map.remove(key);
			else if (value instanceof String && ((String) value).isEmpty())
				map.remove(key);
			else if (value instanceof Map)
				removeEmptyOrNull((Map<String, Object>) value);
		}
	}

	@Override
	public Object get(Object field) {
		if (field == null)
			return null;
		return get(this, field.toString(), false, true);
	}

	public <T> T get(String field) {
		if (field == null)
			return null;
		return get(this, field, false, true);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getAll(String field, Class<T> clazz) {
		List<T> values = new ArrayList<>();
		Collection<Object> all = getAll(this, field);
		for (Object value : all) {
			if (clazz == Long.class && value instanceof Integer)
				value = ((Integer) value).longValue();
			values.add((T) value);
		}
		return values;
	}

	@SuppressWarnings("unchecked")
	private Collection<Object> getAll(Map<String, Object> map, String field) {
		Collection<Object> all = new ArrayList<>();
		if (field.contains(".")) {
			String prefix = field.substring(0, field.lastIndexOf('.'));
			field = field.substring(field.lastIndexOf('.') + 1);
			Collection<Object> allNext = getAll(map, prefix);
			for (Object next : allNext)
				if (next instanceof Map)
					all.addAll(getAll((Map<String, Object>) next, field));
		} else
			all = toArray(map.get(field));
		return all;
	}

	// boolean initialCall is to distinguish between recursive call and initial
	// call, createMissing only applies to recursive calls
	@SuppressWarnings("unchecked")
	private <T> T get(Map<String, Object> map, String field, boolean createMissing, boolean initialCall) {
		if (field.contains(".")) {
			String prefix = field.substring(0, field.lastIndexOf('.'));
			field = field.substring(field.lastIndexOf('.') + 1);
			map = get(map, prefix, createMissing, false);
		}
		Object value = null;
		if (map != null)
			if (map == this)
				value = super.get(field);
			else
				value = map.get(field);
		if (value == null && createMissing && !initialCall)
			value = new HashMap<String, Object>();
		return (T) value;
	}

	public int getArrayLength(String field) {
		Object array = get(this, field, false, true);
		if (array == null)
			array = 0;
		if (array instanceof Collection)
			return ((Collection<?>) array).size();
		return 0;
	}

	public String getString(String field) {
		Object value = get(field);
		if (value == null)
			return null;
		if (value instanceof String[])
			return ((String[]) value)[0];
		return value.toString();
	}

	public long getLong(String field) {
		Object value = get(field);
		if (value == null)
			return 0;
		try {
			if (value instanceof String[])
				return new Double(Double.parseDouble(((String[]) value)[0])).longValue();
			return new Double(Double.parseDouble(value.toString())).longValue();
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public double getDouble(String field) {
		Object value = get(field);
		if (value == null)
			return 0;
		try {
			if (value instanceof String[])
				return Double.parseDouble(((String[]) value)[0]);
			return Double.parseDouble(value.toString());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public boolean getBoolean(String field) {
		Object value = get(field);
		if (value == null)
			return false;
		String stringValue = null;
		if (value instanceof String[])
			stringValue = ((String[]) value)[0].toLowerCase();
		else
			stringValue = value.toString().toLowerCase();
		switch (stringValue) {
		case "true":
			return true;
		case "on":
			return true;
		case "yes":
			return true;
		default:
			return false;
		}
	}

}
