package org.openlca.core.database.internal;

import java.io.BufferedReader;
import java.io.Reader;

/**
 * A parser for SQL scripts.
 */
class ScriptParser {

	private final String COMMENT_PREFIX = "--";
	private final char DELIMITER = ';';
	private ScriptHandler handler;
	private StringBuilder statement;
	private StringBuilder escaped = null;
	private char currentEscaper = ' ';

	public ScriptParser(ScriptHandler handler) {
		this.handler = handler;
	}

	/**
	 * Parse the script from the given reader. The reader is buffered and closed
	 * within this function.
	 */
	public void parse(Reader reader) throws Exception {
		BufferedReader bufferedReader = new BufferedReader(reader);
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			processLine(line, line.trim());
		}
	}

	private void processLine(String line, String trimmedLine) throws Exception {
		if (isStatementPart(trimmedLine)) {
			processStatementPart(line, trimmedLine);
		}
	}

	private boolean isStatementPart(String trimmedLine) {
		return escaped != null || !trimmedLine.isEmpty()
				&& !trimmedLine.startsWith(COMMENT_PREFIX);
	}

	private void processStatementPart(String line, String trimmedLine)
			throws Exception {
		if (escaped != null)
			escaped.append('\n');
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (escaped != null) {
				if (c == '\\') {
					if (peekEquals(line, i, currentEscaper)) {
						escaped.append("\\");
						escaped.append(currentEscaper);
						i++;
						continue;
					}
					if (peekEquals(line, i, '\\')) {
						escaped.append("\\\\");
						i++;
						continue;
					}
				} else if (c == currentEscaper) {
					escaped.append(currentEscaper);
					addStatementPart(escaped.toString());
					escaped = null;
					continue;
				}
				escaped.append(c);
			} else {
				if (c == '\\') {
					if (peekEquals(line, i, '\'')) {
						addStatementPart("\\'");
						i++;
						continue;
					}
					if (peekEquals(line, i, '\"')) {
						addStatementPart("\\\"");
						i++;
						continue;
					}
					if (peekEquals(line, i, '\\')) {
						addStatementPart("\\\\");
						i++;
						continue;
					}
				} else if (c == '\'' || c == '"') {
					escaped = new StringBuilder();
					escaped.append(c);
					currentEscaper = c;
					continue;
				} else if (c == DELIMITER) {
					commitStatement();
					continue;
				}
				addStatementPart(c);
			}
		}
	}

	private boolean peekEquals(String toPeekAt, int pos, char c) {
		if (toPeekAt.length() > pos + 1) {
			if (toPeekAt.charAt(pos + 1) == c) {
				return true;
			}
		}
		return false;
	}

	private void addStatementPart(char c) {
		if (statement == null) {
			statement = new StringBuilder();
		}
		statement.append(c);
	}

	private void addStatementPart(String line) {
		if (statement == null) {
			statement = new StringBuilder();
		}
		statement.append(line);
	}

	private void commitStatement() throws Exception {
		String stmt = statement.toString().trim();
		statement = null;
		if (!stmt.isEmpty())
			handler.statement(stmt);
	}

}
