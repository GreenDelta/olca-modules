package org.openlca.core.indices;

import java.util.List;

/**
 * A helper class for index functions.
 */
class Indices {

	/**
	 * Converts the given list of 64-bit integers in to a SQL string that can be
	 * used in 'in'-queries; e.g. [1,2,3] is converted to (1, 2, 3).
	 */
	public static String asSql(List<Long> ids) {
		if (ids == null)
			return "()";
		StringBuilder b = new StringBuilder();
		b.append('(');
		for (int i = 0; i < ids.size(); i++) {
			b.append(ids.get(i));
			if (i < (ids.size() - 1))
				b.append(',');
		}
		b.append(')');
		return b.toString();
	}

}
