package org.openlca.cloud.model.data;

import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessType;

public class DatasetEntry {

	// if any type
	public String category;
	public String commitId;
	public String commitMessage;
	public String commitTimestamp;
	public String repositoryId;
	public ModelType type;
	public String name;
	public String refId;
	public String tags;
	public String version;

	// if flow
	public FlowType flowType;
	
	// if process
	public ProcessType processType;	
	public String contact;
	public String location;
	public int validFromYear;
	public int validUntilYear;
	
}
