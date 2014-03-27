package org.openlca.core.database;

import java.util.Collections;

import org.openlca.core.model.MappingFile;

public class MappingFileDao extends BaseDao<MappingFile> {

	public MappingFileDao(IDatabase database) {
		super(MappingFile.class, database);
	}

	public MappingFile getForFileName(String fileName) {
		String jpql = "select m from MappingFile m where m.fileName = :fileName";
		return getFirst(jpql, Collections.singletonMap("fileName", fileName));
	}

}
