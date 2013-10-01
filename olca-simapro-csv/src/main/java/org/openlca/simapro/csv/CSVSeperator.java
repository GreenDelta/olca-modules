package org.openlca.simapro.csv;

public enum CSVSeperator {

	TAB('\t', "Tab"), COMMA(',', "Comma"), SEMICOLON(';', "Semicolon");

	private char seperator;
	private String name;

	private CSVSeperator(char seperator, String name) {
		this.seperator = seperator;
		this.name = name;
	}

	public char getSeperator() {
		return seperator;
	}

	public String getName() {
		return name;
	}

}
