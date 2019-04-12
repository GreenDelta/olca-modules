package org.openlca.cloud.model;

import org.openlca.cloud.model.data.Dataset;

public class LibraryRestriction {

	public final String datasetRefId;
	public final String library;
	public final RestrictionType type;
	public Dataset dataset;
	
	public LibraryRestriction(String datasetRefId, String library, RestrictionType type) {
		this.datasetRefId = datasetRefId;
		this.library = library;
		this.type = type;
	}
	
}
