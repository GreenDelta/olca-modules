package org.openlca.simapro.csv;
@Deprecated
class WriterUtils {

	private WriterUtils() {
	}

	/**
	 * Fixes the line ends in multi-line comments: line ends in comments are
	 * indicated by by the ASCII 127 sign (delete character). Additionally, the
	 * comment must end with this delete character. The comment is set in
	 * quotation marks and quotation marks within the comment are replaced by
	 * double quotation marks.
	 * */
	static String comment(String rawComment) {
		if (rawComment == null)
			return null;
		char char127 = 127;
		String comment = rawComment.replaceAll("\"", "\"\"");
		comment = comment.replaceAll("\\r?\\n", "" + char127);
		return "\"" + comment + char127 + "\"";
	}

}
