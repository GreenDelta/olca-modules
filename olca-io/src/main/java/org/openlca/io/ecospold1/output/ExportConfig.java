package org.openlca.io.ecospold1.output;

public class ExportConfig {

	private boolean singleFile = false;
	private boolean schemaValid = false;

	public static ExportConfig getDefault() {
		return new ExportConfig();
	}

	public boolean isSingleFile() {
		return singleFile;
	}

	public void setSingleFile(boolean singleFile) {
		this.singleFile = singleFile;
	}

	public boolean isSchemaValid() {
		return schemaValid;
	}

	public void setSchemaValid(boolean schemaValid) {
		this.schemaValid = schemaValid;
	}

}
