package org.openlca.io.ilcd.output;

import java.io.File;

import org.openlca.core.database.FileStore;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.ClassificationInfo;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.sources.AdministrativeInformation;
import org.openlca.ilcd.sources.DataEntry;
import org.openlca.ilcd.sources.DataSetInformation;
import org.openlca.ilcd.sources.DigitalFileReference;
import org.openlca.ilcd.sources.Publication;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.sources.SourceInformation;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceExport {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final ExportConfig config;
	private String baseUri;
	private org.openlca.core.model.Source source;

	public SourceExport(ExportConfig config) {
		this.config = config;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public Source run(org.openlca.core.model.Source source)
			throws DataStoreException {
		if (config.store.contains(Source.class, source.getRefId()))
			return config.store.get(Source.class, source.getRefId());
		this.source = source;
		log.trace("Run source export with {}", source);
		Source iSource = new Source();
		iSource.setVersion("1.1");
		iSource.setAdministrativeInformation(makeAdminInfo());
		SourceInformation info = new SourceInformation();
		iSource.setSourceInformation(info);
		DataSetInformation dataSetInfo = makeDateSetInfo();
		info.setDataSetInformation(dataSetInfo);
		File extFile = getExternalFile();
		if (extFile == null)
			config.store.put(iSource, source.getRefId());
		else {
			addFileRef(dataSetInfo, extFile);
			config.store.put(iSource, source.getRefId(), extFile);
		}
		return iSource;
	}

	private File getExternalFile() {
		String name = source.getExternalFile();
		if (name == null)
			return null;
		File dbDir = config.db.getFileStorageLocation();
		if (dbDir == null)
			return null;
		String path = FileStore.getPath(ModelType.SOURCE, source.getRefId());
		File docDir = new File(dbDir, path);
		if (!docDir.exists())
			return null;
		File file = new File(docDir, name);
		return file.exists() ? file : null;
	}

	private DataSetInformation makeDateSetInfo() {
		log.trace("Create data set information.");
		DataSetInformation info = new DataSetInformation();
		info.setUUID(source.getRefId());
		LangString.addLabel(info.getShortName(), source.getName(),
				config.ilcdConfig);
		if (source.getDescription() != null) {
			LangString.addFreeText(info.getSourceDescriptionOrComment(),
					source.getDescription(), config.ilcdConfig);
		}
		addTextReference(info);
		CategoryConverter converter = new CategoryConverter();
		ClassificationInfo classInfo = converter
				.getClassificationInformation(source.getCategory());
		info.setClassificationInformation(classInfo);
		return info;
	}

	private void addTextReference(DataSetInformation dataSetInfo) {
		log.trace("Create text reference.");
		String cit = source.getTextReference();
		if (cit == null)
			return;
		if (source.getYear() != null)
			cit += " " + source.getYear();
		dataSetInfo.setSourceCitation(cit);
	}

	private void addFileRef(DataSetInformation info, File extFile) {
		DigitalFileReference fileRef = new DigitalFileReference();
		fileRef.setUri(extFile.getName());
		info.getReferenceToDigitalFile().add(fileRef);
	}

	private AdministrativeInformation makeAdminInfo() {
		AdministrativeInformation info = new AdministrativeInformation();
		DataEntry entry = new DataEntry();
		info.setDataEntryBy(entry);
		entry.setTimeStamp(Out.getTimestamp(source));
		entry.getReferenceToDataSetFormat().add(
				Reference.forIlcdFormat(config.ilcdConfig));
		addPublication(info);
		return info;
	}

	private void addPublication(AdministrativeInformation info) {
		Publication pub = new Publication();
		info.setPublicationAndOwnership(pub);
		pub.setDataSetVersion(Version.asString(source.getVersion()));
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		pub.setPermanentDataSetURI(baseUri + "sources/" + source.getRefId());
	}
}
