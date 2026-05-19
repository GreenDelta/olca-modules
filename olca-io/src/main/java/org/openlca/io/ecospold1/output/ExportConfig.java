package org.openlca.io.ecospold1.output;

public class ExportConfig {

	private boolean singleFile = false;
	private boolean createDefaults = false;

	public static ExportConfig getDefault() {
		return new ExportConfig();
	}

	public boolean isSingleFile() {
		return singleFile;
	}

	public void setSingleFile(boolean singleFile) {
		this.singleFile = singleFile;
	}

	public boolean isCreateDefaults() {
		return createDefaults;
	}

	public void setCreateDefaults(boolean createDefaults) {
		this.createDefaults = createDefaults;
	}

}
