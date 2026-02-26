package org.openlca.sd.model;

import java.util.ArrayList;
import java.util.List;

public sealed interface Subscript {

	static Subscript of(int idx) {
		return idx < 0 ? Empty.instance : new Index(idx);
	}

	static Subscript of(Id id) {
		return id == null ? Empty.instance : new Identifier(id);
	}

	/// Parses a subscript from a string, like "1", "Product", or "*".
	static Subscript of(String s) {
		if (Id.isNil(s))
			return Empty.instance;
		var t = s.strip();

		if ("*".equals(t))
			return Wildcard.instance;

		if (Character.isDigit(t.charAt(0))) {
			try {
				int idx = Integer.parseInt(t);
				return new Index(idx - 1);
			} catch (Exception ignored) {
			}
		}
		return new Identifier(Id.of(t));
	}

	/// Parses a list of subscripts from a string, like "1, Product, *".
	static List<Subscript> parseAllFrom(String s) {
		if (Id.isNil(s))
			return List.of();
		var subs = new ArrayList<Subscript>();
		for (var si : s.strip().split(",")) {
			var sub = of(si);
			subs.add(sub);
		}
		return subs;
	}

	record Index(int value) implements Subscript {

		@Override
		public String toString() {
			return Integer.toString(value);
		}
	}

	record Identifier(Id value) implements Subscript {

		@Override
		public String toString() {
			return value != null ? value.value() : "?";
		}
	}

	record Wildcard() implements Subscript {
		private static final Wildcard instance = new Wildcard();

		@Override
		public String toString() {
			return "*";
		}
	}

	record Empty() implements Subscript {
		private static final Empty instance = new Empty();

		@Override
		public String toString() {
			return "";
		}
	}
}
