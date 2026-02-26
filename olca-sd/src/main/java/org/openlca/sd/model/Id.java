package org.openlca.sd.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Id {

	private static final Id NIL = new Id("*nil*", "");
	private final String label;
	private final String value;

	private Id(String label, String value) {
		this.label = label;
		this.value = value;
	}

	public static Id of(String s) {
		if (isNil(s))
			return NIL;
		var v = s.strip();
		if (v.startsWith("\"") && v.endsWith("\"")) {
			v = v.substring(1, v.length() - 1);
		}

		var wasEscape = new AtomicBoolean(false);
		var val = new StringBuilder();
		Runnable pushEscape = () -> {
			if (!wasEscape.get()) {
				val.append('_');
				wasEscape.set(true);
			}
		};

		for (int pos = 0; pos < v.length(); pos++) {
			char c = v.charAt(pos);

			if (Character.isSpaceChar(c)) {
				pushEscape.run();
				continue;
			}

			if (c == '\\' && pos < (v.length() - 1)) {
				char next = v.charAt(pos + 1);
				if (next == 'n') {
					pushEscape.run();
					pos++;
					continue;
				}
			}

			val.append(c);
			wasEscape.set(false);
		}

		return new Id(s, val.toString().toLowerCase());
	}

	public static Id[] ofAll(String... ss) {
		if (ss == null || ss.length == 0)
			return new Id[0];
		var ids = new Id[ss.length];
		for (int i = 0; i < ss.length; i++) {
			ids[i] = of(ss[i]);
		}
		return ids;
	}

	public static List<Id> allOf(List<String> ss) {
		if (ss == null || ss.isEmpty())
			return List.of();
		var list = new ArrayList<Id>(ss.size());
		for (var s : ss) {
			list.add(Id.of(s));
		}
		return list;
	}

	/// Returns true when the given string is null or blank. This is defined
	/// to be the value of the nil-identifier.
	public static boolean isNil(String s) {
		return s == null || s.isBlank();
	}

	public boolean isNil() {
		return this == NIL;
	}

	public String label() {
		return label;
	}

	public String value() {
		return value;
	}

	@Override
	public String toString() {
		return label;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		return obj instanceof Id other && value.equals(other.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	public boolean matches(String s) {
		return equals(Id.of(s));
	}

}
