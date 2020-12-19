package org.openlca.io.ilcd.output;

import java.io.File;

import org.openlca.core.database.FileStore;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.sources.AdminInfo;
import org.openlca.ilcd.sources.DataSetInfo;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.sources.SourceInfo;
import org.openlca.ilcd.util.Refs;
import org.openlca.io.Xml;
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

	public Source run(org.openlca.core.model.Source source) {
		if (config.store.contains(Source.class, source.refId))
			return config.store.get(Source.class, source.refId);
		this.source = source;
		log.trace("Run source export with {}", source);
		Source iSource = new Source();
		iSource.version = "1.1";
		iSource.adminInfo = makeAdminInfo();
		SourceInfo info = new SourceInfo();
		iSource.sourceInfo = info;
		DataSetInfo dataSetInfo = makeDateSetInfo();
		info.dataSetInfo = dataSetInfo;
		File extFile = getExternalFile();
		if (extFile == null)
			config.store.put(iSource);
		else {
			addFileRef(dataSetInfo, extFile);
			config.store.put(iSource, new File[] { extFile });
		}
		return iSource;
	}

	private File getExternalFile() {
		String name = source.externalFile;
		if (name == null)
			return null;
		File dbDir = config.db.getFileStorageLocation();
		if (dbDir == null)
			return null;
		String path = FileStore.getPath(ModelType.SOURCE, source.refId);
		File docDir = new File(dbDir, path);
		if (!docDir.exists())
			return null;
		File file = new File(docDir, name);
		return file.exists() ? file : null;
	}

	private DataSetInfo makeDateSetInfo() {
		log.trace("Create data set information.");
		DataSetInfo info = new DataSetInfo();
		info.uuid = source.refId;
		LangString.set(info.name, source.name, config.lang);
		if (source.description != null) {
			LangString.set(info.description,
					source.description, config.lang);
		}
		addTextReference(info);
		CategoryConverter converter = new CategoryConverter();
		Classification c = converter.getClassification(
				source.category);
		if (c != null)
			info.classifications.add(c);
		return info;
	}

	private void addTextReference(DataSetInfo dataSetInfo) {
		log.trace("Create text reference.");
		String cit = source.textReference;
		if (cit == null)
			return;
		if (source.year != null)
			cit += " " + source.year;
		dataSetInfo.citation = cit;
	}

	private void addFileRef(DataSetInfo info, File extFile) {
		FileRef fileRef = new FileRef();
		fileRef.uri = "../external_docs/" + extFile.getName();
		info.files.add(fileRef);
	}

	private AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		DataEntry entry = new DataEntry();
		info.dataEntry = entry;
		entry.timeStamp = Xml.calendar(source.lastChange);
		entry.formats.add(Refs.ilcd());
		addPublication(info);
		return info;
	}

	private void addPublication(AdminInfo info) {
		Publication pub = new Publication();
		info.publication = pub;
		pub.version = Version.asString(source.version);
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		pub.uri = baseUri + "sources/" + source.refId;
	}
}
