package org.openlca.core.matrix.cache;

final class CacheUtil {

	private CacheUtil() {
	}

	/**
	 * Converts the given set of 64-bit integers in to a SQL string that can be
	 * used in 'in'-queries; e.g. [1,2,3] is converted to (1,2,3).
	 */
	public static String asSql(Iterable<? extends Long> ids) {
		if (ids == null)
			return "()";
		StringBuilder b = new StringBuilder();
		b.append('(');
		boolean first = true;
		for (Long id : ids) {
			if (!first)
				b.append(',');
			else
				first = false;
			b.append(id);
		}
		b.append(')');
		return b.toString();
	}
}
