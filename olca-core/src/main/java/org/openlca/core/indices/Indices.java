package org.openlca.core.indices;

import java.util.Collection;

/**
 * A helper class for index functions.
 */
class Indices {

	/**
	 * Converts the given list of 64-bit integers in to a SQL string that can be
	 * used in 'in'-queries; e.g. [1,2,3] is converted to (1, 2, 3).
	 */
	public static String asSql(Collection<Long> ids) {
		if (ids == null)
			return "()";
		StringBuilder b = new StringBuilder();
		b.append('(');
		int i = 0;
		for (Long id : ids) {
			b.append(id);
			if (i < (ids.size() - 1))
				b.append(',');
			i++;
		}
		b.append(')');
		return b.toString();
	}

}
