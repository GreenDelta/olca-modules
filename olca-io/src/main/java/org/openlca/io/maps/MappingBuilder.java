package org.openlca.io.maps;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingDao;
import org.openlca.core.model.Mapping;
import org.openlca.io.maps.content.IMappingContent;

import com.google.gson.Gson;

public class MappingBuilder {

	private MappingDao dao;

	public MappingBuilder(IDatabase database) {
		dao = new MappingDao(database);
	}

	public <T extends IMappingContent> ImportMap<T> buildImportMapping( Class<T> contentType,
			String mappingType) {
		Gson gson = new Gson();
		ImportMap<T> map = new ImportMap<T>();
		for (Mapping mapping : dao.getAllForImport(mappingType)) {
			T content = gson.fromJson(mapping.getContent(), contentType);
			map.put(mapping, content);
		}
		return map;
	}

	public <T extends IMappingContent> Map<String, T> buildExportMapping( Class<T> contentType,
			String mappingType) {
		Gson gson = new Gson();
		Map<String, T> map = new HashMap<>();
		for (Mapping mapping : dao.getAllForExport(mappingType)) {
			T content = gson.fromJson(mapping.getContent(), contentType);
			map.put(mapping.getOlcaRefId(), content);
		}
		return map;
	}

}
