package org.openlca.simapro.csv;

public class CsvConfig {

	private String separator;
	private String dateFormat;

	public static CsvConfig getDefault() {
		CsvConfig config = new CsvConfig();
		config.setSeparator(";");
		config.setDateFormat("dd.MM.yyyy");
		return config;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

}
