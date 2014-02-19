package org.openlca.io.maps;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingDao;
import org.openlca.core.model.Mapping;
import org.openlca.io.maps.content.IMappingContent;

import com.google.gson.Gson;

public class MappingImporter {

	private MappingDao dao;

	public MappingImporter(IDatabase database) {
		dao = new MappingDao(database);
	}

	public void addForImport(IMappingContent content, String olcaRefId,
			String mappingType) {
		add(content, olcaRefId, mappingType, true);
	}

	public void addForExport(IMappingContent content, String olcaRefId,
			String mappingType) {
		add(content, olcaRefId, mappingType, false);
	}

	private void add(IMappingContent content, String olcaRefId,
			String mappingType, boolean forImport) {
		// TODO: check if this contains in the table
		Gson gson = new Gson();
		Mapping mapping = new Mapping();
		mapping.setForImport(forImport);
		mapping.setOlcaRefId(olcaRefId);
		mapping.setContent(gson.toJson(content));
		mapping.setMappingType(mappingType);
		dao.insert(mapping);
	}

}
