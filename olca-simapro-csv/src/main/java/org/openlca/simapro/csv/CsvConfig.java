package org.openlca.simapro.csv;

public class CsvConfig {

	private String separator;

	public static CsvConfig getDefault() {
		CsvConfig config = new CsvConfig();
		config.setSeparator(";");
		return config;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

}
