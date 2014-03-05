package org.openlca.simapro.csv.model;

public class SPFileHeader {

	private String simaProVersion;
	private String contentType;
	private String date;
	private String time;
	private String project;
	private String formatVersion;
	private String csvSeparator;
	private String decimalSeparator;
	private String dataSeparator;
	private String shortDateFormat;

	public String getSimaProVersion() {
		return simaProVersion;
	}

	public void setSimaProVersion(String simaProVersion) {
		this.simaProVersion = simaProVersion;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getFormatVersion() {
		return formatVersion;
	}

	public void setFormatVersion(String formatVersion) {
		this.formatVersion = formatVersion;
	}

	public String getCsvSeparator() {
		return csvSeparator;
	}

	public void setCsvSeparator(String csvSeparator) {
		this.csvSeparator = csvSeparator;
	}

	public String getDecimalSeparator() {
		return decimalSeparator;
	}

	public void setDecimalSeparator(String decimalSeparator) {
		this.decimalSeparator = decimalSeparator;
	}

	public String getDataSeparator() {
		return dataSeparator;
	}

	public void setDateSeparator(String dataSeparator) {
		this.dataSeparator = dataSeparator;
	}

	public String getShortDateFormat() {
		return shortDateFormat;
	}

	public void setShortDateFormat(String shortDateFormat) {
		this.shortDateFormat = shortDateFormat;
	}

}
