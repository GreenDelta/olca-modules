package org.openlca.io.ecospold1.output;

public class EcoSpold1Config {

	boolean singleFile = false;
	boolean withDefaults = false;

	// config for product names
	boolean withLocationSuffixes;
	boolean withTypeSuffixes;
	boolean withProcessSuffixes;

	/// If set to `true`, the export will write all process data sets into a
	/// single file.
	public EcoSpold1Config writeSingleFile(boolean singleFile) {
		this.singleFile = singleFile;
		return this;
	}

	/// If set to `true`, the export will write default values for fields that
	/// are required by the schema but cannot be filled by the actual data set.
	public EcoSpold1Config writeDefaultValues(boolean createDefaults) {
		this.withDefaults = createDefaults;
		return this;
	}

}
