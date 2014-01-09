package org.openlca.io.maps;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingDao;
import org.openlca.core.model.Mapping;
import org.openlca.io.maps.content.AbstractMapContent;

public abstract class AbstractMapper {

	protected MappingDao dao;
	protected Map<String, Mapping> importMap = new HashMap<>();
	protected Map<String, Mapping> exportMap = new HashMap<>();

	public AbstractMapper(IDatabase database) {
		dao = new MappingDao(database);
	}

	public boolean add(AbstractMapContent content) {
		Mapping mapping = new Mapping();
		mapping.setForImport(content.isForImport());
		mapping.setExternalContent(content.toJson());
		mapping.setOlcaContent(content.getOlcaId());
		mapping.setMappingType(content.getMapType());
		if (addToMap(mapping, content.getMapType())) {
			dao.insert(mapping);
			return true;
		} else {
			System.err.println("Content already exist: " + content.toJson()
					+ " : " + content.getOlcaId());
			return false;
		}
		// TODO: just add to the map (don't check in the database) if you use
		// several mappers at the same time you can have multiple contents.
	}

	private boolean addToMap(Mapping mapping, String mapType) {
		String key = null;
		if (mapping.isForImport()) {
			key = mapping.getExternalContent() + mapType;
			if (importMap.containsKey(key))
				return false;
			importMap.put(key, mapping);
		} else {
			key = mapping.getOlcaContent() + mapType;
			if (exportMap.containsKey(key))
				return false;
			exportMap.put(key, mapping);
		}
		return true;
	}

	protected void fillMap(String mapType) {
		for (Mapping mapping : dao.getAllForImport(mapType))
			importMap.put(mapping.getExternalContent() + mapType, mapping);
		for (Mapping mapping : dao.getAllForExport(mapType))
			exportMap.put(mapping.getOlcaContent() + mapType, mapping);
	}

	protected AbstractMapContent getForImport(AbstractMapContent content) {
		Mapping mapping = importMap
				.get(content.toJson() + content.getMapType());
		System.out.println(content.toJson() + content.getMapType());
		if (mapping == null)
			return null;
		content.setForImport(true);
		content.setOlcaId(mapping.getOlcaContent());
		return content;
	}

	protected AbstractMapContent getForExport(AbstractMapContent content,
			String olcaId) {
		Mapping mapping = exportMap.get(olcaId + content.getMapType());
		if (mapping == null)
			return null;
		try {
			content.fromJson(mapping.getExternalContent());
		} catch (ParseException e) {
			return null;
		}
		content.setOlcaId(mapping.getOlcaContent());
		content.setForImport(false);
		return content;
	}

}
