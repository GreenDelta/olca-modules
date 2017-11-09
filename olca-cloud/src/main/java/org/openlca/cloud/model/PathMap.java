package org.openlca.cloud.model;

import java.util.HashMap;
import java.util.Map;

class PathMap {

	private static final Map<String, String> map = new HashMap<>();

	static {
		// ProductSystem
		map.put("targetFlowPropertyFactor", "targetFlowProperty");
		// ProcessDocumentation
		map.put("documentation.time", "processDocumentation.timeDescription");
		map.put("documentation.technology", "processDocumentation.technologyDescription");
		map.put("documentation.dataCollectionPeriod", "processDocumentation.dataCollectionDescription");
		map.put("documentation.completeness", "processDocumentation.completenessDescription");
		map.put("documentation.dataSelection", "processDocumentation.dataSelectionDescription");
		map.put("documentation.dataTreatment", "processDocumentation.dataTreatmentDescription");
		map.put("documentation.inventoryMethod", "processDocumentation.inventoryMethodDescription");
		map.put("documentation.modelingConstants", "processDocumentation.modelingConstantsDescription");
		map.put("documentation.sampling", "processDocumentation.samplingDescription");
		map.put("documentation.restrictions", "processDocumentation.restrictionsDescription");
		map.put("documentation.reviewDetails", "processDocumentation.reviewDetails");
		map.put("documentation.project", "processDocumentation.projectDescription");
		map.put("documentation.geography", "processDocumentation.geographyDescription");
		map.put("documentation.copyright", "processDocumentation.copyright");
		map.put("documentation.validFrom", "processDocumentation.validFrom");
		map.put("documentation.validUntil", "processDocumentation.validUntil");
		map.put("documentation.creationDate", "processDocumentation.creationDate");
		map.put("documentation.intendedApplication", "processDocumentation.intendedApplication");
		map.put("documentation.reviewer", "processDocumentation.reviewer()");
		map.put("documentation.dataDocumentor", "processDocumentation.dataDocumentor");
		map.put("documentation.dataGenerator", "processDocumentation.dataGenerator");
		map.put("documentation.dataSetOwner", "processDocumentation.dataSetOwner");
		map.put("documentation.publication", "processDocumentation.publication");
		map.put("documentation.sources", "processDocumentation.sources");		
		// Flow
		map.put("casNumber", "cas");
	}

	static String get(String path) {
		if (!map.containsKey(path))
			return path;
		return map.get(path);
	}

}
