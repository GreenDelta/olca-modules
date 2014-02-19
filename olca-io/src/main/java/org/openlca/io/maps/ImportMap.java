package org.openlca.io.maps;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.model.Mapping;
import org.openlca.io.maps.content.IMappingContent;

public class ImportMap<T extends IMappingContent> {

	private Map<String, String> idMap = new HashMap<>();
	private Map<String, T> contentMap = new HashMap<>();

	public void put(Mapping mapping, T content) {
		idMap.put(content.getKey(), mapping.getOlcaRefId());
		contentMap.put(content.getKey(), content);
	}

	public String getOlcaId(String key) {
		return idMap.get(key);
	}

	public T getContent(String key) {
		return contentMap.get(key);
	}

}
