package org.openlca.sd.model;

import java.util.ArrayList;
import java.util.List;

/// `Id` is used for identifiers of variables, dimensions, and array subscripts
/// in a model. It is immutable and can have two forms: a user-friendly label
/// and a canonical value. For example, `a Var`, `a_Var`, `"a Var"`, `a\nVar`
/// have all the same canonical form `a_var` and thus, are the same identifier
/// in the model.
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
		if (v.length() > 1 && v.startsWith("\"") && v.endsWith("\"")) {
			v = v.substring(1, v.length() - 1);
		}

		var label = new StringBuilder();
		var value = new StringBuilder();
		var wasSeparator = false;

		for (int pos = 0; pos < v.length(); pos++) {
			char c = v.charAt(pos);
			boolean isSeparator = Character.isSpaceChar(c)
				|| (c == '\\' && pos < (v.length() - 1) && v.charAt(pos + 1) == 'n');

			if (isSeparator) {
				if (!wasSeparator) {
					value.append('_');
					label.append(' ');
					wasSeparator = true;
				}
				if (c == '\\') {
					pos++; // skip the encoded new line
				}
				continue;
			}

			value.append(c);
			label.append(c);
			wasSeparator = false;
		}

		return new Id(label.toString(), value.toString().toLowerCase());
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
