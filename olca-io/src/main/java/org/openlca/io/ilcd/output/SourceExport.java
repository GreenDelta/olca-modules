package org.openlca.io.ilcd.output;

import org.openlca.core.model.Source;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.sources.DataSetInformation;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.SourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The export of an openLCA source to an ILCD data set.
 */
public class SourceExport {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private Source source;
	private DataStore dataStore;
	private String baseUri;

	public SourceExport(DataStore dataStore) {
		this.dataStore = dataStore;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public org.openlca.ilcd.sources.Source run(Source source)
			throws DataStoreException {
		this.source = source;
		log.trace("Run source export with {}", source);
		DataSetInformation dataSetInfo = makeDateSetInfo();
		org.openlca.ilcd.sources.Source iSource = SourceBuilder.makeSource()
				.withBaseUri(baseUri).withDataSetInfo(dataSetInfo).getSource();
		dataStore.put(iSource, source.getRefId());
		this.source = null;
		return iSource;
	}

	private DataSetInformation makeDateSetInfo() {
		log.trace("Create data set information.");
		DataSetInformation dataSetInfo = new DataSetInformation();
		dataSetInfo.setUUID(source.getRefId());
		LangString.addLabel(dataSetInfo.getShortName(), source.getName());
		if (source.getDescription() != null) {
			LangString.addFreeText(dataSetInfo.getSourceDescriptionOrComment(),
					source.getDescription());
		}
		addTextReference(dataSetInfo);
		CategoryConverter converter = new CategoryConverter();
		ClassificationInformation classInfo = converter
				.getClassificationInformation(source.getCategory());
		dataSetInfo.setClassificationInformation(classInfo);
		return dataSetInfo;
	}

	private void addTextReference(DataSetInformation dataSetInfo) {
		log.trace("Create text reference.");
		String cit = source.getTextReference();
		if (cit != null) {
			if (source.getYear() != null) {
				cit += " " + source.getYear();
			}
			dataSetInfo.setSourceCitation(cit);
		}
	}

}
