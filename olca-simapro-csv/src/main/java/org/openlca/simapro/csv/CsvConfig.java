package org.openlca.simapro.csv;

public class CsvConfig {

	private char separator;
	private char delimiter;
	private String dateFormat;

	public static CsvConfig getDefault() {
		CsvConfig config = new CsvConfig();
		config.setSeparator(';');
		config.setDelimiter('"');
		config.setDateFormat("dd.MM.yyyy");
		return config;
	}

	public char getSeparator() {
		return separator;
	}

	public void setSeparator(char separator) {
		this.separator = separator;
	}
	
	public char getDelimiter() {
		return delimiter;
	}
	
	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

}
