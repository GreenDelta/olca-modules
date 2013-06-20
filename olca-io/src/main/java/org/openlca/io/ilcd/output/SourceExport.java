/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/

package org.openlca.io.ilcd.output;

import org.openlca.core.database.IDatabase;
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
	private IDatabase database;
	private DataStore dataStore;
	private String baseUri;

	public SourceExport(IDatabase database, DataStore dataStore) {
		this.database = database;
		this.dataStore = dataStore;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public org.openlca.ilcd.sources.Source run(Source source)
			throws DataStoreException {
		log.trace("Run source export with {}", source);
		loadSource(source);
		DataSetInformation dataSetInfo = makeDateSetInfo();
		org.openlca.ilcd.sources.Source iSource = SourceBuilder.makeSource()
				.withBaseUri(baseUri).withDataSetInfo(dataSetInfo).getSource();
		dataStore.put(iSource, source.getId());
		return iSource;
	}

	private void loadSource(Source source) throws DataStoreException {
		try {
			this.source = database.createDao(Source.class).getForId(
					source.getId());
		} catch (Exception e) {
			throw new DataStoreException("Cannot load source from database.", e);
		}
	}

	private DataSetInformation makeDateSetInfo() {
		log.trace("Create data set information.");
		DataSetInformation dataSetInfo = new DataSetInformation();
		dataSetInfo.setUUID(source.getId());
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
