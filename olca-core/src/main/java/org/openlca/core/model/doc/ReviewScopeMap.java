package org.openlca.core.model.doc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class ReviewScopeMap {

	private final HashMap<String, ReviewScope> scopes;

	private ReviewScopeMap(int capacity) {
		this.scopes = new HashMap<>(capacity);
	}

	public ReviewScopeMap() {
		this(5);
	}

	public static ReviewScopeMap fromJson(JsonElement json) {
		if (json == null || !json.isJsonArray())
			return new ReviewScopeMap();
		var array = json.getAsJsonArray();
		var map = new ReviewScopeMap(array.size());
		for (var e : array) {
			ReviewScope.fromJson(e).ifPresent(map::put);
		}
		return map;
	}

	public JsonArray toJson() {
		var array = new JsonArray(scopes.size());
		for (var scope : scopes.values()) {
			array.add(scope.toJson());
		}
		return array;
	}

	public int size() {
		return scopes.size();
	}

	public boolean isEmpty() {
		return scopes.isEmpty();
	}

	public void clear() {
		scopes.clear();
	}

	public void put(ReviewScope scope) {
		if (scope == null)
			return;
		scopes.put(scope.name, scope);
	}

	public ReviewScope get(String name) {
		return scopes.get(name);
	}

	public ReviewScope createIfAbsent(String name) {
		return scopes.computeIfAbsent(name, ReviewScope::new);
	}

	public void remove(String name) {
		scopes.remove(name);
	}

	public Collection<ReviewScope> values() {
		return scopes.values();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof ReviewScopeMap other))
			return false;
		if (this.scopes.size() != other.scopes.size())
			return false;
		for (var e : scopes.entrySet()) {
			var otherVal = other.get(e.getKey());
			if (!Objects.equals(e.getValue(), otherVal))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		for (var s : scopes.values()) {
			hash = 31 * hash + s.hashCode();
		}
		return hash;
	}

}
